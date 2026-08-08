package com.streamsocial.eventtaxonomy.events;

public enum EventType {
    USER_REGISTERED("user_registered"),
    POST_CREATED("post_created"),
    COMMENT_ADDED("comment_added"),
    PROFILE_UPDATED("profile_updated"),
    POST_LIKED("post_liked"),
    CONTENT_SHARED("content_shared"),
    STORY_VIEWED("story_viewed"),
    FOLLOW_INITIATED("follow_initiated"),
    CONTENT_MODERATED("content_moderated"),
    SESSION_EXPIRED("session_expired");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EventType fromValue(String value) {
        for (EventType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown event type: " + value);
    }
}
