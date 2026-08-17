package com.streamsocial.eventtaxonomy.events;

public enum EventType {
    // User Actions (6)
    USER_REGISTRATION("user_registration"),
    USER_LOGIN("user_login"),
    USER_PROFILE_UPDATE("user_profile_update"),
    USER_FOLLOW("user_follow"),
    USER_POST_CREATE("user_post_create"),
    USER_POST_DELETE("user_post_delete"),

    // Content Interactions (3)
    CONTENT_LIKE("content_like"),
    CONTENT_COMMENT("content_comment"),
    CONTENT_SHARE("content_share"),

    // System Events (1)
    SYSTEM_NOTIFICATION("system_notification");

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
