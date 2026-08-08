package com.streamsocial.eventtaxonomy.events;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class EventBus {

    private final Map<String, List<Consumer<BaseEvent>>> subscribers = new ConcurrentHashMap<>();
    private final List<Map<String, Object>> eventStore = new CopyOnWriteArrayList<>();

    public void publish(BaseEvent event) {
        // Store event for replay capability
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("event_id", event.getEventId());
        eventMap.put("timestamp", event.getTimestamp().toString());
        eventMap.put("event_type", event.getEventType());
        eventMap.put("user_id", event.getUserId());
        eventMap.put("data", event.getData());
        eventMap.put("metadata", event.getMetadata());
        eventStore.add(eventMap);

        // Notify all subscribers for this event type
        String eventType = event.getEventType();
        if (subscribers.containsKey(eventType)) {
            for (Consumer<BaseEvent> handler : subscribers.get(eventType)) {
                try {
                    handler.accept(event);
                } catch (Exception e) {
                    System.err.println("Error in event handler: " + e.getMessage());
                }
            }
        }

        System.out.println("✅ Published " + eventType + " event: " + event.getEventId());
    }

    public void subscribe(String eventType, Consumer<BaseEvent> handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
        System.out.println("📡 Subscribed handler to " + eventType);
    }

    public List<Map<String, Object>> getEvents(String eventType, int limit) {
        List<Map<String, Object>> events = eventStore;

        if (eventType != null && !eventType.isEmpty()) {
            events = events.stream()
                    .filter(e -> eventType.equals(e.get("event_type")))
                    .collect(Collectors.toList());
        }

        int fromIndex = Math.max(0, events.size() - limit);
        return events.subList(fromIndex, events.size());
    }

    public Map<String, Integer> getEventStats() {
        Map<String, Integer> stats = new HashMap<>();
        for (Map<String, Object> event : eventStore) {
            String type = (String) event.get("event_type");
            stats.merge(type, 1, Integer::sum);
        }
        return stats;
    }

    public List<Map<String, Object>> getEventStore() {
        return eventStore;
    }
}
