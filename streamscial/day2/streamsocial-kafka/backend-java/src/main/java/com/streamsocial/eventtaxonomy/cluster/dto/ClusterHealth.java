package com.streamsocial.eventtaxonomy.cluster.dto;

/**
 * Cluster-wide health snapshot, per design.md's Data Models section.
 *
 * <p>{@code status} is derived, never client-supplied: {@code "healthy"} iff
 * {@code brokerCount >= 2}, else {@code "degraded"}.
 */
public record ClusterHealth(
        int brokerCount,
        int topicCount,
        String status) {
}
