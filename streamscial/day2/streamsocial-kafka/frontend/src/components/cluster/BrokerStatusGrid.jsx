import React, { useState, useEffect, useRef } from 'react';

const CLUSTER_API = 'http://localhost:8000';

const BrokerStatusGrid = () => {
  const [health, setHealth] = useState(null);
  const [metadata, setMetadata] = useState(null);
  const [error, setError] = useState(null);
  const lastHealthRef = useRef(null);
  const lastMetadataRef = useRef(null);

  const fetchData = async () => {
    try {
      const [healthRes, metadataRes] = await Promise.all([
        fetch(`${CLUSTER_API}/cluster/health`),
        fetch(`${CLUSTER_API}/cluster/metadata`)
      ]);

      if (healthRes.ok) {
        const healthData = await healthRes.json();
        setHealth(healthData);
        lastHealthRef.current = healthData;
      }

      if (metadataRes.ok) {
        const metadataData = await metadataRes.json();
        setMetadata(metadataData);
        lastMetadataRef.current = metadataData;
      }

      setError(null);
    } catch (err) {
      setError(err.message);
      // Preserve last-known state on error
      if (lastHealthRef.current) setHealth(lastHealthRef.current);
      if (lastMetadataRef.current) setMetadata(lastMetadataRef.current);
    }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 3000);
    return () => clearInterval(interval);
  }, []);

  const getStatusColor = (status) => {
    switch (status) {
      case 'healthy': return 'var(--accent-green)';
      case 'degraded': return 'var(--accent-orange)';
      default: return 'var(--accent-red)';
    }
  };

  const getBrokerLeaderCount = (brokerId) => {
    if (!metadata?.partitionLeaders) return 0;
    return metadata.partitionLeaders.filter(p => p.leaderBrokerId === brokerId).length;
  };

  const isBrokerUp = (brokerId) => {
    if (!metadata?.brokers) return false;
    return metadata.brokers.some(b => b.id === brokerId);
  };

  const brokers = [
    { id: 1, name: 'kafka-broker-1', port: 9092 },
    { id: 2, name: 'kafka-broker-2', port: 9093 },
    { id: 3, name: 'kafka-broker-3', port: 9094 }
  ];

  return (
    <div className="cluster-panel">
      <div className="cluster-panel-header">
        <h4>🖥️ Broker Status</h4>
        {health && (
          <span
            className="cluster-status-badge"
            style={{ color: getStatusColor(health.status) }}
          >
            ● {health.status}
          </span>
        )}
      </div>

      {error && !health && (
        <div className="cluster-error">Unable to connect to cluster API</div>
      )}

      {health && (
        <div className="cluster-summary">
          <span>Brokers: <strong>{health.brokerCount}/3</strong></span>
          <span>Topics: <strong>{health.topicCount}</strong></span>
        </div>
      )}

      <div className="broker-grid">
        {brokers.map(broker => {
          const up = isBrokerUp(broker.id);
          const leaderCount = getBrokerLeaderCount(broker.id);
          const isController = metadata?.brokers?.find(b => b.id === broker.id)?.isController;

          return (
            <div
              key={broker.id}
              className={`broker-card ${up ? 'broker-up' : 'broker-down'}`}
            >
              <div className="broker-card-header">
                <span className="broker-status-dot" style={{ background: up ? 'var(--accent-green)' : 'var(--accent-red)' }} />
                <span className="broker-name">Broker {broker.id}</span>
                {isController && <span className="broker-controller-badge">Controller</span>}
              </div>
              <div className="broker-card-body">
                <div className="broker-metric">
                  <span className="broker-metric-label">Port</span>
                  <span className="broker-metric-value">{broker.port}</span>
                </div>
                <div className="broker-metric">
                  <span className="broker-metric-label">Leader Partitions</span>
                  <span className="broker-metric-value">{leaderCount}</span>
                </div>
                <div className="broker-metric">
                  <span className="broker-metric-label">Status</span>
                  <span className="broker-metric-value" style={{ color: up ? 'var(--accent-green)' : 'var(--accent-red)' }}>
                    {up ? 'UP' : 'DOWN'}
                  </span>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default BrokerStatusGrid;
