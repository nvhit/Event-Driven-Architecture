import React, { useState, useEffect, useRef } from 'react';

const CLUSTER_API = 'http://localhost:8000';

const ConsumerLoadChart = () => {
  const [stats, setStats] = useState(null);
  const [error, setError] = useState(null);
  const lastStatsRef = useRef(null);

  const fetchStats = async () => {
    try {
      const res = await fetch(`${CLUSTER_API}/consumers/stats`);
      if (res.ok) {
        const data = await res.json();
        setStats(data);
        lastStatsRef.current = data;
        setError(null);
      }
    } catch (err) {
      setError(err.message);
      if (lastStatsRef.current) setStats(lastStatsRef.current);
    }
  };

  useEffect(() => {
    fetchStats();
    const interval = setInterval(fetchStats, 3000);
    return () => clearInterval(interval);
  }, []);

  const getMaxPartitions = () => {
    if (!stats?.consumers) return 9;
    return Math.max(9, ...stats.consumers.map(c => c.assignedPartitions));
  };

  const totalPartitions = stats?.consumers
    ? stats.consumers.reduce((sum, c) => sum + c.assignedPartitions, 0)
    : 0;

  const totalLag = stats?.consumers
    ? stats.consumers.reduce((sum, c) => sum + c.totalLag, 0)
    : 0;

  return (
    <div className="cluster-panel">
      <div className="cluster-panel-header">
        <h4>📊 Consumer Load</h4>
        {stats && (
          <span className="cluster-meta-text">
            Group: {stats.groupId || 'streamsocial-cluster-consumers'}
          </span>
        )}
      </div>

      {error && !stats && (
        <div className="cluster-error">Unable to fetch consumer stats</div>
      )}

      {stats?.consumers && (
        <>
          <div className="consumer-summary">
            <span>Consumers: <strong>{stats.consumers.length}</strong></span>
            <span>Total Partitions: <strong>{totalPartitions}</strong></span>
            <span>Total Lag: <strong>{totalLag}</strong></span>
          </div>

          <div className="consumer-chart">
            {stats.consumers.map((consumer, idx) => {
              const barWidth = (consumer.assignedPartitions / getMaxPartitions()) * 100;
              const colors = ['var(--accent-cyan)', 'var(--accent-green)', 'var(--accent-purple)'];

              return (
                <div key={consumer.consumerId} className="consumer-row">
                  <div className="consumer-label">
                    <span className="consumer-id">{consumer.consumerId}</span>
                  </div>
                  <div className="consumer-bar-container">
                    <div
                      className="consumer-bar"
                      style={{
                        width: `${barWidth}%`,
                        background: colors[idx % colors.length]
                      }}
                    />
                    <span className="consumer-bar-value">
                      {consumer.assignedPartitions} partitions
                    </span>
                  </div>
                  <div className="consumer-lag">
                    lag: {consumer.totalLag}
                  </div>
                </div>
              );
            })}
          </div>
        </>
      )}
    </div>
  );
};

export default ConsumerLoadChart;
