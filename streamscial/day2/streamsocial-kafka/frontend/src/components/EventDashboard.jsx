import React from 'react';

const EventDashboard = ({ events, stats }) => {
  const formatTimestamp = (timestamp) => {
    const date = new Date(timestamp);
    return date.toLocaleTimeString('en-US', { hour12: false }) + '.' + String(date.getMilliseconds()).padStart(3, '0');
  };

  const getEventIcon = (eventType) => {
    const icons = {
      // User Actions (6)
      'user_registration': '🆕',
      'user_login': '🔑',
      'user_profile_update': '✏️',
      'user_follow': '👥',
      'user_post_create': '📝',
      'user_post_delete': '🗑️',
      // Content Interactions (3)
      'content_like': '❤️',
      'content_comment': '💬',
      'content_share': '🔄',
      // System Events (1)
      'system_notification': '🔔'
    };
    return icons[eventType] || '📊';
  };

  const getEventCategory = (eventType) => {
    if (eventType?.startsWith('user_')) return 'user';
    if (eventType?.startsWith('content_')) return 'content';
    if (eventType?.startsWith('system_')) return 'system';
    return 'unknown';
  };

  const getCategoryColor = (category) => {
    switch (category) {
      case 'user': return '#4caf50';
      case 'content': return '#2196f3';
      case 'system': return '#ff9800';
      default: return '#9e9e9e';
    }
  };

  return (
    <div className="event-dashboard">
      {/* Stats Row */}
      <div className="stats-section">
        <h3>📊 Metrics Overview</h3>
        <div className="stats-grid">
          <div className="stat-card">
            <h4>Total Events</h4>
            <span className="stat-number">{stats.total_events || 0}</span>
          </div>
          <div className="stat-card">
            <h4>Active Feeds</h4>
            <span className="stat-number">{stats.feed_users || 0}</span>
          </div>
          <div className="stat-card">
            <h4>Notifications</h4>
            <span className="stat-number">{stats.notification_users || 0}</span>
          </div>
        </div>

        {/* Event Breakdown */}
        {stats.event_stats && Object.keys(stats.event_stats).length > 0 && (
          <div className="event-types">
            <h4>Event Distribution</h4>
            {Object.entries(stats.event_stats).map(([type, count]) => (
              <div key={type} className="event-type-stat">
                <span>
                  <span style={{ 
                    display: 'inline-block', 
                    width: 8, 
                    height: 8, 
                    borderRadius: '50%', 
                    backgroundColor: getCategoryColor(getEventCategory(type)),
                    marginRight: 6 
                  }}></span>
                  {getEventIcon(type)} {type}
                </span>
                <span className="count">{count}</span>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Event Stream */}
      <div className="events-section">
        <h3>🔄 Event Stream (Real-time)</h3>
        <div className="events-list">
          {events.length === 0 ? (
            <div style={{ color: 'var(--text-disabled)', fontSize: '0.82rem', padding: '20px', textAlign: 'center' }}>
              No events yet. Publish an event to get started.
            </div>
          ) : (
            [...events].reverse().map((event, index) => {
              const category = getEventCategory(event.event_type);
              return (
                <div key={event.event_id || index} className={`event-item event-category-${category}`}>
                  <div className="event-header">
                    <span className="event-icon">
                      {getEventIcon(event.event_type)}
                    </span>
                    <span className="event-type">{event.event_type}</span>
                    <span className="event-time">
                      {formatTimestamp(event.timestamp)}
                    </span>
                  </div>
                  <div className="event-details">
                    <span>user: {event.user_id}</span>
                    <span>id: {event.event_id?.slice(0, 8)}</span>
                    {event.session_id && <span>session: {event.session_id?.slice(0, 8)}</span>}
                  </div>
                  {event.data && Object.keys(event.data).length > 0 && (
                    <div className="event-data">
                      {JSON.stringify(event.data, null, 2)}
                    </div>
                  )}
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
};

export default EventDashboard;
