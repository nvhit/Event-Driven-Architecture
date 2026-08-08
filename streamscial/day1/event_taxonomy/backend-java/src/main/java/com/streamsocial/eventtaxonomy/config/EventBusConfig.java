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
        eventBus.subscribe(EventType.POST_CREATED.getValue(), feedHandler::handlePostCreated);
        eventBus.subscribe(EventType.POST_LIKED.getValue(), feedHandler::handlePostLiked);
        eventBus.subscribe(EventType.FOLLOW_INITIATED.getValue(), notificationHandler::handleFollowInitiated);
        eventBus.subscribe(EventType.COMMENT_ADDED.getValue(), notificationHandler::handleCommentAdded);
    }
}
