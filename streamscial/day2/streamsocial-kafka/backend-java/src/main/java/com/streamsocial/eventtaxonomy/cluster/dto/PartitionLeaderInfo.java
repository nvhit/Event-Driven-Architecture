package com.streamsocial.eventtaxonomy.cluster.dto;

import java.util.List;

/**
 * Per-partition leadership/replication snapshot as reported by
 * {@code AdminClient.describeTopics()}, per design.md's Data Models section.
 */
public record PartitionLeaderInfo(
        String topic,
        int partition,
        int leaderBrokerId,
        List<Integer> replicaBrokerIds,
        List<Integer> inSyncReplicaBrokerIds) {
}
