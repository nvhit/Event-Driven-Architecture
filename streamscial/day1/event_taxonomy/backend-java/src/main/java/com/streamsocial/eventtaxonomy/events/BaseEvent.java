package com.streamsocial.eventtaxonomy.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    @JsonProperty("event_type")
    private String eventType;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("session_id")
    @Builder.Default
    private String sessionId = UUID.randomUUID().toString();

    private Map<String, Object> data;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    public static BaseEvent create(EventType type, String userId, Map<String, Object> data) {
        return BaseEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(type.getValue())
                .timestamp(LocalDateTime.now())
                .userId(userId)
                .sessionId(UUID.randomUUID().toString())
                .data(data)
                .metadata(new HashMap<>())
                .build();
    }
}
