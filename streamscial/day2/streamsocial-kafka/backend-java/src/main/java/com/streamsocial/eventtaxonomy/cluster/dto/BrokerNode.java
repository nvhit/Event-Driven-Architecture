package com.streamsocial.eventtaxonomy.cluster.dto;

/**
 * A single broker node as reported by {@code AdminClient.describeCluster()},
 * per design.md's Data Models section.
 */
public record BrokerNode(
        int id,
        String host,
        int port,
        boolean isController) {
}
