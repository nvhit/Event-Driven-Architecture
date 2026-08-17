package com.streamsocial.eventtaxonomy.config;

import com.streamsocial.eventtaxonomy.events.EventBus;
import com.streamsocial.eventtaxonomy.events.EventType;
import com.streamsocial.eventtaxonomy.handlers.FeedHandler;
import com.streamsocial.eventtaxonomy.handlers.NotificationHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class EventBusConfig {

    private final EventBus eventBus;
    private final FeedHandler feedHandler;
    private final NotificationHandler notificationHandler;

    public EventBusConfig(EventBus eventBus, FeedHandler feedHandler, NotificationHandler notificationHandler) {
        this.eventBus = eventBus;
        this.feedHandler = feedHandler;
        this.notificationHandler = notificationHandler;
    }

    @PostConstruct
    public void setupSubscriptions() {
        // Feed handler subscriptions
        eventBus.subscribe(EventType.USER_POST_CREATE.getValue(), feedHandler::handlePostCreated);
        eventBus.subscribe(EventType.CONTENT_LIKE.getValue(), feedHandler::handlePostLiked);
        eventBus.subscribe(EventType.USER_POST_DELETE.getValue(), feedHandler::handlePostDeleted);

        // Notification handler subscriptions
        eventBus.subscribe(EventType.USER_FOLLOW.getValue(), notificationHandler::handleFollowUser);
        eventBus.subscribe(EventType.CONTENT_COMMENT.getValue(), notificationHandler::handleCommentAdded);
        eventBus.subscribe(EventType.CONTENT_LIKE.getValue(), notificationHandler::handleContentLiked);
        eventBus.subscribe(EventType.USER_REGISTRATION.getValue(), notificationHandler::handleUserRegistration);
    }
}
