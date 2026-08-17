package com.streamsocial.eventtaxonomy.cluster.dto;

import java.util.List;

/**
 * Full cluster metadata snapshot (brokers + per-partition leadership), per
 * design.md's Data Models section.
 */
public record ClusterMetadata(
        List<BrokerNode> brokers,
        List<PartitionLeaderInfo> partitionLeaders) {
}
