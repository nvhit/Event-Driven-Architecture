package com.streamsocial.eventtaxonomy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FollowUserRequest {

    @JsonProperty("follower_id")
    private String followerId;

    @JsonProperty("followed_user_id")
    private String followedUserId;
}
