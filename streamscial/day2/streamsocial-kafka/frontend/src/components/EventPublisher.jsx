import React, { useState } from 'react';

const API_BASE = 'http://localhost:8080/api/v1/events';

const EventPublisher = ({ onEventPublished }) => {
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [postContent, setPostContent] = useState('');
  const [commentContent, setCommentContent] = useState('');
  const [activeTab, setActiveTab] = useState('user');

  const currentUser = {
    name: 'streamsocial_dev',
    avatar: '🧑‍💻',
    userId: 'user_001'
  };

  const publishEvent = async (endpoint, data) => {
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
      });

      if (response.ok) {
        const result = await response.json();
        setMessage(`✅ Event published: ${result.event_id?.slice(0, 8)}`);
        onEventPublished();
      } else {
        setMessage('❌ Failed to publish event');
      }
    } catch (error) {
      setMessage(`❌ Error: ${error.message}`);
    }
    setLoading(false);
    setTimeout(() => setMessage(''), 3000);
  };

  // ========== USER ACTIONS ==========
  const handleRegister = () => {
    publishEvent('/user/register', {
      username: `user_${Date.now().toString(36)}`,
      email: `user_${Date.now().toString(36)}@streamsocial.com`
    });
  };

  const handleLogin = () => {
    publishEvent('/user/login', {
      user_id: currentUser.userId,
      username: currentUser.name,
      ip_address: '192.168.1.' + Math.floor(Math.random() * 255)
    });
  };

  const handleProfileUpdate = () => {
    publishEvent('/user/profile-update', {
      user_id: currentUser.userId,
      fields_updated: ['avatar', 'bio']
    });
  };

  const handleFollow = () => {
    publishEvent('/user/follow', {
      follower_id: currentUser.userId,
      followed_user_id: `user_${Math.floor(Math.random() * 1000)}`
    });
  };

  const handleCreatePost = () => {
    const content = postContent || `Post from ${new Date().toLocaleTimeString()}`;
    publishEvent('/user/post-create', {
      user_id: currentUser.userId,
      content,
      media_urls: []
    });
    setPostContent('');
  };

  const handleDeletePost = () => {
    publishEvent('/user/post-delete', {
      user_id: currentUser.userId,
      post_id: `post_${Math.floor(Math.random() * 1000)}`,
      reason: 'user_requested'
    });
  };

  // ========== CONTENT INTERACTIONS ==========
  const handleLike = () => {
    publishEvent('/content/like', {
      user_id: currentUser.userId,
      post_id: `post_${Math.floor(Math.random() * 1000)}`
    });
  };

  const handleComment = () => {
    const content = commentContent || 'Great post! 🔥';
    publishEvent('/content/comment', {
      user_id: currentUser.userId,
      post_id: `post_${Math.floor(Math.random() * 1000)}`,
      post_owner_id: `user_${Math.floor(Math.random() * 1000)}`,
      content
    });
    setCommentContent('');
  };

  const handleShare = () => {
    publishEvent('/content/share', {
      user_id: currentUser.userId,
      post_id: `post_${Math.floor(Math.random() * 1000)}`,
      share_target: 'timeline'
    });
  };

  // ========== SYSTEM EVENTS ==========
  const handleSystemNotification = () => {
    publishEvent('/system/notification', {
      notification_type: 'maintenance',
      message: 'System maintenance scheduled',
      target_users: [currentUser.userId]
    });
  };

  return (
    <div className="ig-publisher">
      {/* Profile Header */}
      <div className="ig-profile">
        <div className="ig-avatar">
          <div className="ig-avatar-ring">
            <span className="ig-avatar-emoji">{currentUser.avatar}</span>
          </div>
        </div>
        <div className="ig-profile-info">
          <div className="ig-username">{currentUser.name}</div>
          <div className="ig-bio">Event-Driven Architecture Demo</div>
        </div>
      </div>

      {/* Tab Navigation - 3 categories from slide */}
      <div className="ig-tabs">
        <button
          className={`ig-tab ${activeTab === 'user' ? 'active' : ''}`}
          onClick={() => setActiveTab('user')}
        >
          🟢 User Actions
        </button>
        <button
          className={`ig-tab ${activeTab === 'content' ? 'active' : ''}`}
          onClick={() => setActiveTab('content')}
        >
          🔵 Content
        </button>
        <button
          className={`ig-tab ${activeTab === 'system' ? 'active' : ''}`}
          onClick={() => setActiveTab('system')}
        >
          🟠 System
        </button>
      </div>

      {/* Content Area */}
      <div className="ig-content">
        {activeTab === 'user' && (
          <div className="ig-actions-grid">
            <button onClick={handleRegister} disabled={loading} className="ig-grid-btn">
              <div className="ig-grid-icon">🆕</div>
              <span>Register</span>
              <small>user_registration</small>
            </button>
            <button onClick={handleLogin} disabled={loading} className="ig-grid-btn">
              <div className="ig-grid-icon">🔑</div>
              <span>Login</span>
              <small>user_login</small>
            </button>
            <button onClick={handleProfileUpdate} disabled={loading} className="ig-grid-btn">
              <div className="ig-grid-icon">✏️</div>
              <span>Update Profile</span>
              <small>user_profile_update</small>
            </button>
            <button onClick={handleFollow} disabled={loading} className="ig-grid-btn">
              <div className="ig-grid-icon">👥</div>
              <span>Follow</span>
              <small>user_follow</small>
            </button>
            <button onClick={handleCreatePost} disabled={loading} className="ig-grid-btn">
              <div className="ig-grid-icon">📝</div>
              <span>Create Post</span>
              <small>user_post_create</small>
            </button>
            <button onClick={handleDeletePost} disabled={loading} className="ig-grid-btn">
              <div className="ig-grid-icon">🗑️</div>
              <span>Delete Post</span>
              <small>user_post_delete</small>
            </button>
          </div>
        )}

        {activeTab === 'content' && (
          <div className="ig-actions-grid">
            <button onClick={handleLike} disabled={loading} className="ig-grid-btn">
              <div className="ig-grid-icon">❤️</div>
              <span>Like</span>
              <small>content_like</small>
            </button>
            <button onClick={handleComment} disabled={loading} className="ig-grid-btn">
              <div className="ig-grid-icon">💬</div>
              <span>Comment</span>
              <small>content_comment</small>
            </button>
            <button onClick={handleShare} disabled={loading} className="ig-grid-btn">
              <div className="ig-grid-icon">🔄</div>
              <span>Share</span>
              <small>content_share</small>
            </button>

            {/* Quick input for post/comment */}
            <div className="ig-quick-input" style={{ gridColumn: '1 / -1' }}>
              <input
                type="text"
                placeholder="Write a post..."
                value={postContent}
                onChange={(e) => setPostContent(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleCreatePost()}
                className="ig-caption-input"
              />
              <input
                type="text"
                placeholder="Add a comment..."
                value={commentContent}
                onChange={(e) => setCommentContent(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleComment()}
                className="ig-comment-input"
              />
            </div>
          </div>
        )}

        {activeTab === 'system' && (
          <div className="ig-actions-grid">
            <button onClick={handleSystemNotification} disabled={loading} className="ig-grid-btn">
              <div className="ig-grid-icon">🔔</div>
              <span>Notification</span>
              <small>system_notification</small>
            </button>
          </div>
        )}
      </div>

      {/* Toast Message */}
      {message && (
        <div className="ig-toast">
          {message}
        </div>
      )}
    </div>
  );
};

export default EventPublisher;
