import React, { useState } from 'react';

const EventPublisher = ({ onEventPublished }) => {
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [postContent, setPostContent] = useState('');
  const [commentContent, setCommentContent] = useState('');
  const [activeTab, setActiveTab] = useState('feed');

  const currentUser = {
    name: 'streamsocial_dev',
    avatar: '🧑‍💻',
    followers: 1024,
    following: 256,
    posts: 42
  };

  const publishEvent = async (eventType, data) => {
    setLoading(true);
    try {
      const response = await fetch(`/api/events/${eventType}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
      });

      if (response.ok) {
        setMessage(`Event published successfully`);
        onEventPublished();
      } else {
        setMessage(`Failed to publish event`);
      }
    } catch (error) {
      setMessage(`Error: ${error.message}`);
    }
    setLoading(false);
    setTimeout(() => setMessage(''), 3000);
  };

  const handleCreatePost = () => {
    const userId = currentUser.name;
    const content = postContent || `Photo from ${new Date().toLocaleTimeString()}`;
    publishEvent('post', { user_id: userId, content });
    setPostContent('');
  };

  const handleLikePost = () => {
    const userId = currentUser.name;
    const postId = `post_${Math.floor(Math.random() * 1000)}`;
    publishEvent('like', { user_id: userId, post_id: postId });
  };

  const handleFollowUser = () => {
    const followerId = currentUser.name;
    const followedId = `user_${Math.floor(Math.random() * 1000)}`;
    publishEvent('follow', {
      follower_id: followerId,
      followed_user_id: followedId
    });
  };

  const handleAddComment = () => {
    const userId = currentUser.name;
    const postId = `post_${Math.floor(Math.random() * 1000)}`;
    const ownerId = `user_${Math.floor(Math.random() * 1000)}`;
    const content = commentContent || `Nice! 🔥`;
    publishEvent('comment', {
      user_id: userId,
      post_id: postId,
      post_owner_id: ownerId,
      content
    });
    setCommentContent('');
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



      {/* Tab Navigation */}
      <div className="ig-tabs">
        <button
          className={`ig-tab ${activeTab === 'feed' ? 'active' : ''}`}
          onClick={() => setActiveTab('feed')}
        >
          <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><rect x="2" y="2" width="9" height="9"/><rect x="13" y="2" width="9" height="9"/><rect x="2" y="13" width="9" height="9"/><rect x="13" y="13" width="9" height="9"/></svg>
          Feed
        </button>
        <button
          className={`ig-tab ${activeTab === 'actions' ? 'active' : ''}`}
          onClick={() => setActiveTab('actions')}
        >
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
          Actions
        </button>
      </div>

      {/* Content Area */}
      <div className="ig-content">
        {activeTab === 'feed' && (
          <div className="ig-create-post">
            {/* New Post Card */}
            <div className="ig-post-card">
              <div className="ig-post-header">
                <span className="ig-post-avatar">{currentUser.avatar}</span>
                <span className="ig-post-user">{currentUser.name}</span>
                <span className="ig-post-time">now</span>
              </div>
              <div className="ig-post-image">
                <div className="ig-post-placeholder">
                  📸
                  <span>Share a moment</span>
                </div>
              </div>
              <div className="ig-post-actions-bar">
                <button className="ig-action-btn" onClick={handleLikePost} disabled={loading}>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
                </button>
                <button className="ig-action-btn" onClick={() => setActiveTab('actions')} disabled={loading}>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                </button>
                <button className="ig-action-btn" onClick={handleFollowUser} disabled={loading}>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>
                </button>
              </div>

              {/* Caption/Post input */}
              <div className="ig-caption-area">
                <input
                  type="text"
                  className="ig-caption-input"
                  placeholder="Write a caption..."
                  value={postContent}
                  onChange={(e) => setPostContent(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleCreatePost()}
                />
                <button
                  className="ig-share-btn"
                  onClick={handleCreatePost}
                  disabled={loading}
                >
                  Share
                </button>
              </div>

              {/* Comment input */}
              <div className="ig-comment-area">
                <input
                  type="text"
                  className="ig-comment-input"
                  placeholder="Add a comment..."
                  value={commentContent}
                  onChange={(e) => setCommentContent(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleAddComment()}
                />
                <button
                  className="ig-comment-btn"
                  onClick={handleAddComment}
                  disabled={loading || !commentContent}
                >
                  Post
                </button>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'actions' && (
          <div className="ig-actions-grid">
            <button onClick={handleCreatePost} disabled={loading} className="ig-grid-btn">
              <div className="ig-grid-icon">📝</div>
              <span>New Post</span>
            </button>
            <button onClick={handleLikePost} disabled={loading} className="ig-grid-btn">
              <div className="ig-grid-icon">❤️</div>
              <span>Like</span>
            </button>
            <button onClick={handleFollowUser} disabled={loading} className="ig-grid-btn">
              <div className="ig-grid-icon">👥</div>
              <span>Follow</span>
            </button>
            <button onClick={handleAddComment} disabled={loading} className="ig-grid-btn">
              <div className="ig-grid-icon">💬</div>
              <span>Comment</span>
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
