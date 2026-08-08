package com.streamsocial.eventtaxonomy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ProfileUpdateRequest {

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("fields_updated")
    private List<String> fieldsUpdated;
}
