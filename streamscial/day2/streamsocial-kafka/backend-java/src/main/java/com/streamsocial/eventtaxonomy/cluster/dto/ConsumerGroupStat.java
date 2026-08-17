package com.streamsocial.eventtaxonomy.cluster.dto;

/**
 * Per-consumer-instance stat snapshot for the cluster consumer group.
 *
 * <p>Mirrors design.md's Data Models section:
 * {@code ConsumerGroupStat(String consumerId, int assignedPartitions, long totalLag)}.
 *
 * <p>{@code totalLag} is populated as {@code 0L} by {@code ConsumerStatsRegistry}
 * (no live broker connection available there); real lag computation from
 * consumer group offsets vs. log end offsets is handled by the
 * AdminClient-backed {@code ClusterInspector} (task 5).
 */
public record ConsumerGroupStat(
        String consumerId,
        int assignedPartitions,
        long totalLag) {
}
