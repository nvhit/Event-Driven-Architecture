package com.streamsocial.eventtaxonomy.handlers;

import com.streamsocial.eventtaxonomy.events.BaseEvent;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationHandler {

    private final Map<String, List<Map<String, Object>>> notifications = new ConcurrentHashMap<>();

    public void handleFollowUser(BaseEvent event) {
        String followedUser = (String) event.getData().get("followed_user_id");
        String follower = event.getUserId();

        addNotification(followedUser, Map.of(
                "type", "new_follower",
                "message", "User " + follower + " started following you",
                "timestamp", event.getTimestamp().toString(),
                "read", false
        ));
        System.out.println("🔔 Follow notification sent to user " + followedUser);
    }

    public void handleCommentAdded(BaseEvent event) {
        String postOwner = (String) event.getData().get("post_owner_id");
        String commenter = event.getUserId();

        if (postOwner != null && !postOwner.equals(commenter)) {
            addNotification(postOwner, Map.of(
                    "type", "comment",
                    "message", "User " + commenter + " commented on your post",
                    "timestamp", event.getTimestamp().toString(),
                    "read", false
            ));
            System.out.println("💬 Comment notification sent to user " + postOwner);
        }
    }

    public void handleContentLiked(BaseEvent event) {
        String postId = (String) event.getData().get("post_id");
        String liker = event.getUserId();

        // In a real system, we'd look up the post owner
        System.out.println("❤️ Like notification - " + liker + " liked post " + postId);
    }

    public void handleUserRegistration(BaseEvent event) {
        String userId = event.getUserId();
        String username = (String) event.getData().get("username");

        addNotification(userId, Map.of(
                "type", "welcome",
                "message", "Welcome to StreamSocial, " + username + "!",
                "timestamp", event.getTimestamp().toString(),
                "read", false
        ));
        System.out.println("🎉 Welcome notification sent to new user " + username);
    }

    private void addNotification(String userId, Map<String, Object> notification) {
        notifications.computeIfAbsent(userId, k -> Collections.synchronizedList(new ArrayList<>()));
        notifications.get(userId).add(new HashMap<>(notification));
    }

    public List<Map<String, Object>> getNotifications(String userId) {
        return notifications.getOrDefault(userId, Collections.emptyList());
    }

    public Map<String, List<Map<String, Object>>> getAllNotifications() {
        return notifications;
    }
}
