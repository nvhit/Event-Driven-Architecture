import React from 'react';

const EventDashboard = ({ events, stats }) => {
  const formatTimestamp = (timestamp) => {
    const date = new Date(timestamp);
    return date.toLocaleTimeString('en-US', { hour12: false }) + '.' + String(date.getMilliseconds()).padStart(3, '0');
  };

  const getEventIcon = (eventType) => {
    const icons = {
      'post_created': '📝',
      'post_liked': '❤️',
      'follow_initiated': '👥',
      'comment_added': '💬',
      'user_registered': '🆕',
      'content_shared': '🔄',
      'story_viewed': '👁️',
      'profile_updated': '✏️',
      'content_moderated': '🛡️',
      'session_expired': '⏰'
    };
    return icons[eventType] || '📊';
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
                <span>{getEventIcon(type)} {type}</span>
                <span className="count">{count}</span>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Event Stream */}
      <div className="events-section">
        <h3>🔄 Event Stream</h3>
        <div className="events-list">
          {events.length === 0 ? (
            <div style={{ color: 'var(--text-disabled)', fontSize: '0.82rem', padding: '20px', textAlign: 'center' }}>
              No events yet. Publish an event to get started.
            </div>
          ) : (
            [...events].reverse().map((event, index) => (
              <div key={event.event_id || index} className="event-item">
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
                </div>
                {event.data && Object.keys(event.data).length > 0 && (
                  <div className="event-data">
                    {JSON.stringify(event.data, null, 2)}
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};

export default EventDashboard;
