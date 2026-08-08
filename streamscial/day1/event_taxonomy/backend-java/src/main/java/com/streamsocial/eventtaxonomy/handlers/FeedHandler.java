package com.streamsocial.eventtaxonomy.handlers;

import com.streamsocial.eventtaxonomy.events.BaseEvent;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FeedHandler {

    private final Map<String, List<Map<String, Object>>> feedData = new ConcurrentHashMap<>();

    public void handlePostCreated(BaseEvent event) {
        String userId = event.getUserId();
        feedData.computeIfAbsent(userId, k -> Collections.synchronizedList(new ArrayList<>()));

        Map<String, Object> postData = new HashMap<>();
        postData.put("post_id", event.getData().get("post_id"));
        postData.put("content", event.getData().get("content"));
        postData.put("timestamp", event.getTimestamp().toString());
        postData.put("likes", 0);

        feedData.get(userId).add(postData);
        System.out.println("📝 Feed updated - new post by user " + userId);
    }

    public void handlePostLiked(BaseEvent event) {
        String postId = (String) event.getData().get("post_id");

        for (List<Map<String, Object>> userPosts : feedData.values()) {
            for (Map<String, Object> post : userPosts) {
                if (postId.equals(post.get("post_id"))) {
                    int likes = (int) post.getOrDefault("likes", 0);
                    post.put("likes", likes + 1);
                }
            }
        }
        System.out.println("❤️ Post " + postId + " liked by " + event.getUserId());
    }

    public void handlePostDeleted(BaseEvent event) {
        String postId = (String) event.getData().get("post_id");
        String userId = event.getUserId();

        List<Map<String, Object>> userPosts = feedData.get(userId);
        if (userPosts != null) {
            userPosts.removeIf(post -> postId.equals(post.get("post_id")));
        }
        System.out.println("🗑️ Post " + postId + " deleted by user " + userId);
    }

    public List<Map<String, Object>> getUserFeed(String userId) {
        return feedData.getOrDefault(userId, Collections.emptyList());
    }

    public Map<String, List<Map<String, Object>>> getAllFeeds() {
        return feedData;
    }
}
