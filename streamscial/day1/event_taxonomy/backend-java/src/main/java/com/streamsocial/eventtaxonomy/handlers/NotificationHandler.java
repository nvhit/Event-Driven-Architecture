package com.streamsocial.eventtaxonomy.handlers;

import com.streamsocial.eventtaxonomy.events.BaseEvent;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationHandler {

    private final Map<String, List<Map<String, Object>>> notifications = new ConcurrentHashMap<>();

    public void handleFollowInitiated(BaseEvent event) {
        String followedUser = (String) event.getData().get("followed_user_id");
        String follower = event.getUserId();

        notifications.computeIfAbsent(followedUser, k -> Collections.synchronizedList(new ArrayList<>()));

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "new_follower");
        notification.put("message", "User " + follower + " started following you");
        notification.put("timestamp", event.getTimestamp().toString());
        notification.put("read", false);

        notifications.get(followedUser).add(notification);
        System.out.println("🔔 Notification sent to user " + followedUser);
    }

    public void handleCommentAdded(BaseEvent event) {
        String postOwner = (String) event.getData().get("post_owner_id");
        String commenter = event.getUserId();

        if (postOwner != null && !postOwner.equals(commenter)) {
            notifications.computeIfAbsent(postOwner, k -> Collections.synchronizedList(new ArrayList<>()));

            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "comment");
            notification.put("message", "User " + commenter + " commented on your post");
            notification.put("timestamp", event.getTimestamp().toString());
            notification.put("read", false);

            notifications.get(postOwner).add(notification);
            System.out.println("💬 Comment notification sent to user " + postOwner);
        }
    }

    public List<Map<String, Object>> getNotifications(String userId) {
        return notifications.getOrDefault(userId, Collections.emptyList());
    }

    public Map<String, List<Map<String, Object>>> getAllNotifications() {
        return notifications;
    }
}
