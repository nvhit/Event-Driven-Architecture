package com.streamsocial.eventtaxonomy.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseEvent {

    @JsonProperty("event_id")
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    @Builder.Default
    private Instant timestamp = Instant.now();

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("user_id")
    private String userId;

    private Map<String, Object> data;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    public static BaseEvent create(EventType type, String userId, Map<String, Object> data) {
        return BaseEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .eventType(type.getValue())
                .userId(userId)
                .data(data)
                .metadata(new HashMap<>())
                .build();
    }
}
