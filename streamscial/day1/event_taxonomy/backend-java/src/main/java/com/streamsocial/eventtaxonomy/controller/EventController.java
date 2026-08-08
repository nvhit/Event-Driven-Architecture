package com.streamsocial.eventtaxonomy.controller;

import com.streamsocial.eventtaxonomy.dto.AddCommentRequest;
import com.streamsocial.eventtaxonomy.dto.CreatePostRequest;
import com.streamsocial.eventtaxonomy.dto.FollowUserRequest;
import com.streamsocial.eventtaxonomy.dto.LikePostRequest;
import com.streamsocial.eventtaxonomy.events.BaseEvent;
import com.streamsocial.eventtaxonomy.events.EventBus;
import com.streamsocial.eventtaxonomy.events.EventType;
import com.streamsocial.eventtaxonomy.handlers.FeedHandler;
import com.streamsocial.eventtaxonomy.handlers.NotificationHandler;
import com.streamsocial.eventtaxonomy.websocket.EventWebSocketHandler;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventBus eventBus;
    private final FeedHandler feedHandler;
    private final NotificationHandler notificationHandler;
    private final EventWebSocketHandler webSocketHandler;

    public EventController(EventBus eventBus, FeedHandler feedHandler,
                           NotificationHandler notificationHandler,
                           EventWebSocketHandler webSocketHandler) {
        this.eventBus = eventBus;
        this.feedHandler = feedHandler;
        this.notificationHandler = notificationHandler;
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping("/events/post")
    public Map<String, Object> createPost(@RequestBody CreatePostRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("post_id", UUID.randomUUID().toString());
        data.put("content", request.getContent());
        data.put("media_urls", request.getMediaUrls());

        BaseEvent event = BaseEvent.create(EventType.POST_CREATED, request.getUserId(), data);

        eventBus.publish(event);
        broadcastEvent(event);

        return Map.of("status", "success", "event_id", event.getEventId());
    }

    @PostMapping("/events/like")
    public Map<String, Object> likePost(@RequestBody LikePostRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("post_id", request.getPostId());

        BaseEvent event = BaseEvent.create(EventType.POST_LIKED, request.getUserId(), data);

        eventBus.publish(event);
        broadcastEvent(event);

        return Map.of("status", "success", "event_id", event.getEventId());
    }

    @PostMapping("/events/follow")
    public Map<String, Object> followUser(@RequestBody FollowUserRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("followed_user_id", request.getFollowedUserId());

        BaseEvent event = BaseEvent.create(EventType.FOLLOW_INITIATED, request.getFollowerId(), data);

        eventBus.publish(event);
        broadcastEvent(event);

        return Map.of("status", "success", "event_id", event.getEventId());
    }

    @PostMapping("/events/comment")
    public Map<String, Object> addComment(@RequestBody AddCommentRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("post_id", request.getPostId());
        data.put("post_owner_id", request.getPostOwnerId());
        data.put("content", request.getContent());

        BaseEvent event = BaseEvent.create(EventType.COMMENT_ADDED, request.getUserId(), data);

        eventBus.publish(event);
        broadcastEvent(event);

        return Map.of("status", "success", "event_id", event.getEventId());
    }

    @GetMapping("/events")
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
        webSocketHandler.broadcast(message);
    }
}
