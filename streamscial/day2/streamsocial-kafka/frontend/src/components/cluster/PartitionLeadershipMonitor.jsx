import React, { useState, useEffect, useRef } from 'react';

const CLUSTER_API = 'http://localhost:8000';

const PartitionLeadershipMonitor = () => {
  const [metadata, setMetadata] = useState(null);
  const [error, setError] = useState(null);
  const lastMetadataRef = useRef(null);
  const [selectedTopic, setSelectedTopic] = useState(null);

  const fetchMetadata = async () => {
    try {
      const res = await fetch(`${CLUSTER_API}/cluster/metadata`);
      if (res.ok) {
        const data = await res.json();
        setMetadata(data);
        lastMetadataRef.current = data;
        setError(null);
      }
    } catch (err) {
      setError(err.message);
      if (lastMetadataRef.current) setMetadata(lastMetadataRef.current);
    }
  };

  useEffect(() => {
    fetchMetadata();
    const interval = setInterval(fetchMetadata, 3000);
    return () => clearInterval(interval);
  }, []);

  const getTopics = () => {
    if (!metadata?.partitionLeaders) return [];
    const topics = [...new Set(metadata.partitionLeaders.map(p => p.topic))];
    return topics.sort();
  };

  const getPartitionsForTopic = (topic) => {
    if (!metadata?.partitionLeaders) return [];
    return metadata.partitionLeaders
      .filter(p => p.topic === topic)
      .sort((a, b) => a.partition - b.partition);
  };

  const getBrokerColor = (brokerId) => {
    const colors = {
      1: 'var(--accent-cyan)',
      2: 'var(--accent-green)',
      3: 'var(--accent-purple)'
    };
    return colors[brokerId] || 'var(--text-secondary)';
  };

  const topics = getTopics();
  const displayTopic = selectedTopic || topics[0];

  return (
    <div className="cluster-panel">
      <div className="cluster-panel-header">
        <h4>🗺️ Partition Leadership</h4>
      </div>

      {error && !metadata && (
        <div className="cluster-error">Unable to fetch partition data</div>
      )}

      {topics.length > 0 && (
        <>
          <div className="partition-topic-tabs">
            {topics.map(topic => (
              <button
                key={topic}
                className={`partition-topic-tab ${displayTopic === topic ? 'active' : ''}`}
                onClick={() => setSelectedTopic(topic)}
              >
                {topic}
              </button>
            ))}
          </div>

          <div className="partition-grid">
            <div className="partition-grid-header">
              <span>Partition</span>
              <span>Leader</span>
              <span>Replicas</span>
              <span>ISR</span>
            </div>
            {getPartitionsForTopic(displayTopic).map(p => (
              <div key={`${p.topic}-${p.partition}`} className="partition-row">
                <span className="partition-id">P{p.partition}</span>
                <span
                  className="partition-leader"
                  style={{ color: getBrokerColor(p.leaderBrokerId) }}
                >
                  Broker {p.leaderBrokerId}
                </span>
                <span className="partition-replicas">
                  {p.replicaBrokerIds?.map(id => (
                    <span
                      key={id}
                      className="replica-badge"
                      style={{ background: getBrokerColor(id) }}
                    >
                      {id}
                    </span>
                  ))}
                </span>
                <span className="partition-isr">
                  {p.inSyncReplicaBrokerIds?.map(id => (
                    <span
                      key={id}
                      className="isr-badge"
                      style={{ borderColor: getBrokerColor(id) }}
                    >
                      {id}
                    </span>
                  ))}
                </span>
              </div>
            ))}
          </div>

          {/* Legend */}
          <div className="partition-legend">
            <span style={{ color: 'var(--accent-cyan)' }}>● Broker 1</span>
            <span style={{ color: 'var(--accent-green)' }}>● Broker 2</span>
            <span style={{ color: 'var(--accent-purple)' }}>● Broker 3</span>
          </div>
        </>
      )}
    </div>
  );
};

export default PartitionLeadershipMonitor;
