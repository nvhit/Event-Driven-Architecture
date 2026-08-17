package com.streamsocial.eventtaxonomy.producer;

import com.streamsocial.eventtaxonomy.events.BaseEvent;
import com.streamsocial.eventtaxonomy.events.EventBus;
import com.streamsocial.eventtaxonomy.events.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Event Producer - Ghi lại hành động user → Publish event lên Kafka
 * Dùng userId làm partition key → đảm bảo thứ tự event cùng user
 *
 * Khi Kafka available: publish lên Kafka topic "streamsocial-events"
 * Khi Kafka unavailable: fallback vào in-memory EventBus
 */
@Service
public class EventProducerService {

    private static final String TOPIC = "streamsocial-events";

    @Autowired(required = false)
    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    private final EventBus eventBus;

    public EventProducerService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Publish event - qua Kafka nếu available, luôn publish vào EventBus local
     */
    public BaseEvent publishEvent(EventType type, String userId, Map<String, Object> data) {
        BaseEvent event = BaseEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(type.getValue())
                .timestamp(LocalDateTime.now())
                .userId(userId)
                .sessionId(UUID.randomUUID().toString())
                .data(data)
                .build();

        // Always publish to local EventBus (for handlers + real-time dashboard)
        eventBus.publish(event);

        // Publish to Kafka if available
        if (kafkaTemplate != null) {
            kafkaTemplate.send(TOPIC, userId, event);
            System.out.println("📤 Published to Kafka: " + type.getValue() + " | event_id=" + event.getEventId());
        }

        return event;
    }
}
