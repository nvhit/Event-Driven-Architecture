package com.streamsocial.eventtaxonomy.cluster.dto;

/**
 * Result of a cluster producer send, per design.md's Requirement 2.5 /
 * Requirement 4.8 ("return the resulting partition/offset") and the
 * {@code EventController} reference in the "Java: REST controllers"
 * section.
 *
 * <p>{@code success} distinguishes the 200 case (populated {@code topic},
 * {@code partition}, {@code offset}) from the 503 case, where the send
 * failed (e.g. after exhausting retries due to fewer than
 * {@code min.insync.replicas} available); in that case {@code partition}
 * and {@code offset} are {@code -1}.
 */
public record RecordMetadataResponse(
        boolean success,
        String topic,
        int partition,
        long offset) {
}
