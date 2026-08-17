import React, { useState } from 'react';

const CLUSTER_API = 'http://localhost:8000';

const FaultToleranceControls = () => {
  const [results, setResults] = useState({});
  const [loading, setLoading] = useState({});

  const simulateFailure = async (brokerName) => {
    setLoading(prev => ({ ...prev, [brokerName]: true }));
    try {
      const res = await fetch(`${CLUSTER_API}/cluster/simulate-failure`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ broker_name: brokerName })
      });

      if (res.ok) {
        const data = await res.json();
        setResults(prev => ({
          ...prev,
          [brokerName]: { success: true, message: `${data.status}: ${data.broker}` }
        }));
      } else {
        const errorText = await res.text();
        setResults(prev => ({
          ...prev,
          [brokerName]: { success: false, message: `Error ${res.status}: ${errorText}` }
        }));
      }
    } catch (err) {
      setResults(prev => ({
        ...prev,
        [brokerName]: { success: false, message: err.message }
      }));
    }
    setLoading(prev => ({ ...prev, [brokerName]: false }));

    // Clear result after 5 seconds
    setTimeout(() => {
      setResults(prev => {
        const copy = { ...prev };
        delete copy[brokerName];
        return copy;
      });
    }, 5000);
  };

  const brokers = [
    { name: 'kafka-broker-1', id: 1 },
    { name: 'kafka-broker-2', id: 2 },
    { name: 'kafka-broker-3', id: 3 }
  ];

  return (
    <div className="cluster-panel">
      <div className="cluster-panel-header">
        <h4>⚡ Fault Tolerance Controls</h4>
      </div>

      <p className="cluster-panel-description">
        Simulate broker failures to test cluster resilience. Stopping a broker triggers leader election for its partitions.
      </p>

      <div className="fault-controls-grid">
        {brokers.map(broker => (
          <div key={broker.name} className="fault-control-card">
            <div className="fault-control-header">
              <span className="fault-broker-name">Broker {broker.id}</span>
              <span className="fault-broker-container">{broker.name}</span>
            </div>
            <button
              className="fault-stop-btn"
              onClick={() => simulateFailure(broker.name)}
              disabled={loading[broker.name]}
            >
              {loading[broker.name] ? '⏳ Stopping...' : '🛑 Stop Broker'}
            </button>
            {results[broker.name] && (
              <div className={`fault-result ${results[broker.name].success ? 'success' : 'error'}`}>
                {results[broker.name].message}
              </div>
            )}
          </div>
        ))}
      </div>

      <div className="fault-info">
        <p>💡 After stopping a broker, observe:</p>
        <ul>
          <li>Broker Status Grid shows broker as DOWN</li>
          <li>Partition leadership re-elected to surviving brokers</li>
          <li>Consumer load rebalanced across remaining instances</li>
          <li>Cluster status changes to "degraded" (2/3 brokers)</li>
        </ul>
      </div>
    </div>
  );
};

export default FaultToleranceControls;
