package com.streamsocial.eventtaxonomy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AddCommentRequest {

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("post_id")
    private String postId;

    @JsonProperty("post_owner_id")
    private String postOwnerId;

    private String content;
}
