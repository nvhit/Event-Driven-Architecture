package com.streamsocial.eventtaxonomy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreatePostRequest {

    @JsonProperty("user_id")
    private String userId;

    private String content;

    @JsonProperty("media_urls")
    private List<String> mediaUrls = new ArrayList<>();
}
