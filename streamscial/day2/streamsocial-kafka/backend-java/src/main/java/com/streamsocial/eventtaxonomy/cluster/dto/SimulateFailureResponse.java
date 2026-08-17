package com.streamsocial.eventtaxonomy.cluster.dto;

/**
 * Response body for {@code POST /cluster/simulate-failure}, per design.md's
 * Data Models section: {@code SimulateFailureResponse(String status, String broker)}.
 *
 * <p>Both fields serialize as camelCase JSON, consistent with the other
 * cluster response DTOs in this package (e.g. {@code ClusterHealth}'s
 * {@code brokerCount}/{@code topicCount}); only the request's
 * {@code broker_name} needs the snake_case mapping per Requirement 4.6.
 */
public record SimulateFailureResponse(
        String status,
        String broker) {
}
