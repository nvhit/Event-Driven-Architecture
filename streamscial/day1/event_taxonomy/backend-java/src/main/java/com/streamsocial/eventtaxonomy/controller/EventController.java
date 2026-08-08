package com.streamsocial.eventtaxonomy.controller;

import com.streamsocial.eventtaxonomy.dto.*;
import com.streamsocial.eventtaxonomy.events.BaseEvent;
import com.streamsocial.eventtaxonomy.events.EventBus;
import com.streamsocial.eventtaxonomy.events.EventType;
import com.streamsocial.eventtaxonomy.handlers.FeedHandler;
import com.streamsocial.eventtaxonomy.handlers.NotificationHandler;
import com.streamsocial.eventtaxonomy.producer.EventProducerService;
import com.streamsocial.eventtaxonomy.websocket.EventWebSocketHandler;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventProducerService producerService;
    private final EventBus eventBus;
    private final FeedHandler feedHandler;
    private final NotificationHandler notificationHandler;
    private final EventWebSocketHandler webSocketHandler;

    public EventController(EventProducerService producerService,
                           EventBus eventBus,
                           FeedHandler feedHandler,
                           NotificationHandler notificationHandler,
                           EventWebSocketHandler webSocketHandler) {
        this.producerService = producerService;
        this.eventBus = eventBus;
        this.feedHandler = feedHandler;
        this.notificationHandler = notificationHandler;
        this.webSocketHandler = webSocketHandler;
    }

    // ========== USER ACTIONS (6) ==========

    @PostMapping("/user/register")
    public Map<String, Object> registerUser(@RequestBody UserRegistrationRequest request) {
        String userId = UUID.randomUUID().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("username", request.getUsername());
        data.put("email", request.getEmail());

        BaseEvent event = producerService.publishEvent(EventType.USER_REGISTRATION, userId, data);
        broadcastEvent(event);

        return Map.of("success", true, "user_id", userId, "event_id", event.getEventId());
    }

    @PostMapping("/user/login")
    public Map<String, Object> loginUser(@RequestBody UserLoginRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", request.getUsername());
        data.put("ip_address", request.getIpAddress());

        BaseEvent event = producerService.publishEvent(EventType.USER_LOGIN, request.getUserId(), data);
        broadcastEvent(event);

        return Map.of("success", true, "event_id", event.getEventId());
    }

    @PostMapping("/user/profile-update")
    public Map<String, Object> updateProfile(@RequestBody ProfileUpdateRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("fields_updated", request.getFieldsUpdated());

        BaseEvent event = producerService.publishEvent(EventType.USER_PROFILE_UPDATE, request.getUserId(), data);
        broadcastEvent(event);

        return Map.of("success", true, "event_id", event.getEventId());
    }

    @PostMapping("/user/follow")
    public Map<String, Object> followUser(@RequestBody FollowUserRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("followed_user_id", request.getFollowedUserId());

        BaseEvent event = producerService.publishEvent(EventType.USER_FOLLOW, request.getFollowerId(), data);
        broadcastEvent(event);

        return Map.of("success", true, "event_id", event.getEventId());
    }

    @PostMapping("/user/post-create")
    public Map<String, Object> createPost(@RequestBody CreatePostRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("post_id", UUID.randomUUID().toString());
        data.put("content", request.getContent());
        data.put("media_urls", request.getMediaUrls());

        BaseEvent event = producerService.publishEvent(EventType.USER_POST_CREATE, request.getUserId(), data);
        broadcastEvent(event);

        return Map.of("success", true, "event_id", event.getEventId(), "post_id", data.get("post_id"));
    }

    @PostMapping("/user/post-delete")
    public Map<String, Object> deletePost(@RequestBody DeletePostRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("post_id", request.getPostId());
        data.put("reason", request.getReason());

        BaseEvent event = producerService.publishEvent(EventType.USER_POST_DELETE, request.getUserId(), data);
        broadcastEvent(event);

        return Map.of("success", true, "event_id", event.getEventId());
    }

    // ========== CONTENT INTERACTIONS (3) ==========

    @PostMapping("/content/like")
    public Map<String, Object> likeContent(@RequestBody LikePostRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("post_id", request.getPostId());
        data.put("action", "like");

        BaseEvent event = producerService.publishEvent(EventType.CONTENT_LIKE, request.getUserId(), data);
        broadcastEvent(event);

        return Map.of("success", true, "event_id", event.getEventId());
    }

    @PostMapping("/content/comment")
    public Map<String, Object> commentContent(@RequestBody AddCommentRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("post_id", request.getPostId());
        data.put("post_owner_id", request.getPostOwnerId());
        data.put("content", request.getContent());

        BaseEvent event = producerService.publishEvent(EventType.CONTENT_COMMENT, request.getUserId(), data);
        broadcastEvent(event);

        return Map.of("success", true, "event_id", event.getEventId());
    }

    @PostMapping("/content/share")
    public Map<String, Object> shareContent(@RequestBody ShareContentRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("post_id", request.getPostId());
        data.put("share_target", request.getShareTarget());

        BaseEvent event = producerService.publishEvent(EventType.CONTENT_SHARE, request.getUserId(), data);
        broadcastEvent(event);

        return Map.of("success", true, "event_id", event.getEventId());
    }

    // ========== SYSTEM EVENTS (1) ==========

    @PostMapping("/system/notification")
    public Map<String, Object> systemNotification(@RequestBody SystemNotificationRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("notification_type", request.getNotificationType());
        data.put("message", request.getMessage());
        data.put("target_users", request.getTargetUsers());

        BaseEvent event = producerService.publishEvent(EventType.SYSTEM_NOTIFICATION, "system", data);
        broadcastEvent(event);

        return Map.of("success", true, "event_id", event.getEventId());
    }

    // ========== QUERY ENDPOINTS ==========

    @GetMapping("/recent")
    public Map<String, Object> getRecentEvents(
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        List<Map<String, Object>> events = eventBus.getEvents(null, limit);
        return Map.of("success", true, "events", events);
    }

    @GetMapping("")
    public Map<String, Object> getEvents(
            @RequestParam(value = "event_type", required = false) String eventType,
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        List<Map<String, Object>> events = eventBus.getEvents(eventType, limit);
        return Map.of("events", events);
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("event_stats", eventBus.getEventStats());
        result.put("total_events", eventBus.getEventStore().size());
        result.put("feed_users", feedHandler.getAllFeeds().size());
        result.put("notification_users", notificationHandler.getAllNotifications().size());
        return result;
    }

    @GetMapping("/feed/{userId}")
    public Map<String, Object> getUserFeed(@PathVariable String userId) {
        return Map.of("feed", feedHandler.getUserFeed(userId));
    }

    @GetMapping("/notifications/{userId}")
    public Map<String, Object> getUserNotifications(@PathVariable String userId) {
        return Map.of("notifications", notificationHandler.getNotifications(userId));
    }

    private void broadcastEvent(BaseEvent event) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "event_published");
        message.put("event_type", event.getEventType());
        message.put("event_id", event.getEventId());
        message.put("user_id", event.getUserId());
        message.put("timestamp", event.getTimestamp().toString());
        webSocketHandler.broadcast(message);
    }
}
