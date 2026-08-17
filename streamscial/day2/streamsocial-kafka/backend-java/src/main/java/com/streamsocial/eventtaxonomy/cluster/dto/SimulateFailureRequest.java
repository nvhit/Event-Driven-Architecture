package com.streamsocial.eventtaxonomy.cluster.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body for {@code POST /cluster/simulate-failure}, per design.md's
 * Data Models section: {@code SimulateFailureRequest(String brokerName)}.
 *
 * <p>The JSON wire format uses snake_case {@code broker_name} (per
 * Requirement 4.6 and the frontend's {@code FaultToleranceControls} fetch
 * call), mapped onto the camelCase {@code brokerName} record component via
 * {@link JsonProperty}, consistent with how existing (non-cluster) request
 * DTOs such as {@code CreatePostRequest} map snake_case JSON fields (e.g.
 * {@code user_id}) onto camelCase Java fields in this codebase.
 */
public record SimulateFailureRequest(
        @JsonProperty("broker_name") String brokerName) {
}
