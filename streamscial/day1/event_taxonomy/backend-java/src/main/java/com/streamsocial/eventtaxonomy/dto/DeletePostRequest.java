package com.streamsocial.eventtaxonomy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DeletePostRequest {

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("post_id")
    private String postId;

    private String reason;
}
