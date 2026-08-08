package com.streamsocial.eventtaxonomy;

import com.streamsocial.eventtaxonomy.events.BaseEvent;
import com.streamsocial.eventtaxonomy.events.EventBus;
import com.streamsocial.eventtaxonomy.events.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

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

        BaseEvent event = BaseEvent.create(EventType.POST_CREATED, "test_user", data);

        assertEquals(EventType.POST_CREATED.getValue(), event.getEventType());
        assertEquals("test_user", event.getUserId());
        assertEquals("Test post", event.getData().get("content"));
        assertNotNull(event.getEventId());
    }

    @Test
    void testEventBusPublish() {
        EventBus bus = new EventBus();
        Map<String, Object> data = new HashMap<>();
        data.put("email", "test@example.com");

        BaseEvent event = BaseEvent.create(EventType.USER_REGISTERED, "test_user", data);
        bus.publish(event);

        assertEquals(1, bus.getEventStore().size());
        assertEquals(EventType.USER_REGISTERED.getValue(), bus.getEventStore().get(0).get("event_type"));
    }

    @Test
    void testEventSubscription() {
        EventBus bus = new EventBus();
        List<BaseEvent> receivedEvents = new ArrayList<>();

        bus.subscribe(EventType.POST_CREATED.getValue(), receivedEvents::add);

        Map<String, Object> data = new HashMap<>();
        data.put("content", "Test");

        BaseEvent event = BaseEvent.create(EventType.POST_CREATED, "test_user", data);
        bus.publish(event);

        assertEquals(1, receivedEvents.size());
        assertEquals(EventType.POST_CREATED.getValue(), receivedEvents.get(0).getEventType());
    }
}
