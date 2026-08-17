package com.streamsocial.eventtaxonomy;

import com.streamsocial.eventtaxonomy.events.BaseEvent;
import com.streamsocial.eventtaxonomy.events.EventBus;
import com.streamsocial.eventtaxonomy.events.EventType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EventTaxonomyApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testEventCreation() {
        Map<String, Object> data = new HashMap<>();
        data.put("content", "Test post");

        BaseEvent event = BaseEvent.create(EventType.USER_POST_CREATE, "test_user", data);

        assertEquals(EventType.USER_POST_CREATE.getValue(), event.getEventType());
        assertEquals("test_user", event.getUserId());
        assertEquals("Test post", event.getData().get("content"));
        assertNotNull(event.getEventId());
        assertNotNull(event.getSessionId());
    }

    @Test
    void testEventBusPublish() {
        EventBus bus = new EventBus();
        Map<String, Object> data = new HashMap<>();
        data.put("email", "test@example.com");
        data.put("username", "testuser");

        BaseEvent event = BaseEvent.create(EventType.USER_REGISTRATION, "test_user", data);
        bus.publish(event);

        assertEquals(1, bus.getEventStore().size());
        assertEquals(EventType.USER_REGISTRATION.getValue(), bus.getEventStore().get(0).get("event_type"));
    }

    @Test
    void testEventSubscription() {
        EventBus bus = new EventBus();
        List<BaseEvent> receivedEvents = new ArrayList<>();

        bus.subscribe(EventType.CONTENT_LIKE.getValue(), receivedEvents::add);

        Map<String, Object> data = new HashMap<>();
        data.put("post_id", "post_123");
        data.put("action", "like");

        BaseEvent event = BaseEvent.create(EventType.CONTENT_LIKE, "test_user", data);
        bus.publish(event);

        assertEquals(1, receivedEvents.size());
        assertEquals(EventType.CONTENT_LIKE.getValue(), receivedEvents.get(0).getEventType());
    }

    @Test
    void testAllEventTypes() {
        // Verify all 10 event types from the taxonomy
        assertEquals(10, EventType.values().length);

        // User Actions (6)
        assertNotNull(EventType.USER_REGISTRATION);
        assertNotNull(EventType.USER_LOGIN);
        assertNotNull(EventType.USER_PROFILE_UPDATE);
        assertNotNull(EventType.USER_FOLLOW);
        assertNotNull(EventType.USER_POST_CREATE);
        assertNotNull(EventType.USER_POST_DELETE);

        // Content Interactions (3)
        assertNotNull(EventType.CONTENT_LIKE);
        assertNotNull(EventType.CONTENT_COMMENT);
        assertNotNull(EventType.CONTENT_SHARE);

        // System Events (1)
        assertNotNull(EventType.SYSTEM_NOTIFICATION);
    }

    @Test
    void testEventTypeFromValue() {
        assertEquals(EventType.USER_REGISTRATION, EventType.fromValue("user_registration"));
        assertEquals(EventType.CONTENT_LIKE, EventType.fromValue("content_like"));
        assertEquals(EventType.SYSTEM_NOTIFICATION, EventType.fromValue("system_notification"));
    }
}
