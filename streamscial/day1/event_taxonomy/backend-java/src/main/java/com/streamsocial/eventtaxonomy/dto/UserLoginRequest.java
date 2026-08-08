package com.streamsocial.eventtaxonomy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserLoginRequest {

    @JsonProperty("user_id")
    private String userId;

    private String username;

    @JsonProperty("ip_address")
    private String ipAddress;
}
