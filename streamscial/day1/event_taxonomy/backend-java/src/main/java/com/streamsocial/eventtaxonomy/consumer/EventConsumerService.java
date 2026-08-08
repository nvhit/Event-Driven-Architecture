package com.streamsocial.eventtaxonomy.consumer;

import com.streamsocial.eventtaxonomy.events.BaseEvent;
import com.streamsocial.eventtaxonomy.events.EventType;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Event Consumer - Subscribe topic → Nhận & xử lý event theo loại
 * Mỗi service chỉ consume event mà nó quan tâm.
 * 
 * Only active when Kafka is enabled (spring.kafka.enabled=true)
 */
@Service
@Profile("kafka")
public class EventConsumerService {

    private final List<BaseEvent> processedEvents = new CopyOnWriteArrayList<>();

    @KafkaListener(topics = "streamsocial-events", groupId = "streamsocial-consumers")
    public void consume(BaseEvent event) {
        processedEvents.add(event);
        handleEvent(event);
        System.out.println("📥 Consumed: " + event.getEventType() + " | event_id=" + event.getEventId());
    }

    private void handleEvent(BaseEvent event) {
        String type = event.getEventType();

        if (EventType.CONTENT_LIKE.getValue().equals(type)) {
            System.out.println("  → Analytics: track engagement for post");
        } else if (EventType.USER_REGISTRATION.getValue().equals(type)) {
            System.out.println("  → Welcome: send welcome email to " + event.getData().get("username"));
        } else if (EventType.CONTENT_COMMENT.getValue().equals(type)) {
            System.out.println("  → Notification: comment on post " + event.getData().get("post_id"));
        } else if (EventType.USER_FOLLOW.getValue().equals(type)) {
            System.out.println("  → Notification: new follower");
        } else {
            System.out.println("  → Processed event: " + type);
        }
    }

    public List<BaseEvent> getRecentEvents(int limit) {
        int size = processedEvents.size();
        int fromIndex = Math.max(0, size - limit);
        return processedEvents.subList(fromIndex, size);
    }

    public int getTotalProcessed() {
        return processedEvents.size();
    }
}
