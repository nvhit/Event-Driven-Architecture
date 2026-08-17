package com.streamsocial.eventtaxonomy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SystemNotificationRequest {

    @JsonProperty("notification_type")
    private String notificationType;

    private String message;

    @JsonProperty("target_users")
    private List<String> targetUsers;
}
