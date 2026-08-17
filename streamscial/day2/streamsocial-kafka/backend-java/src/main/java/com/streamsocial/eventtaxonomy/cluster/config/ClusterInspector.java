package com.streamsocial.eventtaxonomy.cluster.config;

import com.streamsocial.eventtaxonomy.cluster.dto.ClusterHealth;
import com.streamsocial.eventtaxonomy.cluster.dto.ClusterMetadata;
import com.streamsocial.eventtaxonomy.cluster.dto.ConsumerGroupStat;

import java.util.List;

/**
 * Read-only cluster state used by both health checks and the frontend
 * dashboard, per design.md's "Component: AdminClient-backed Cluster
 * Introspection" section.
 *
 * <p>Implementations must never propagate AdminClient exceptions/timeouts to
 * callers: per the "Error Handling" section, an AdminClient timeout during a
 * health check is caught and surfaced as a degraded/zero state instead of an
 * unhandled exception.
 */
public interface ClusterInspector {

    /**
     * Preconditions: none.
     * Postconditions: returns brokerCount == number of live nodes returned by
     *   describeCluster().nodes() and topicCount == number of known topics;
     *   status == "healthy" iff brokerCount >= 2, else "degraded". Never
     *   throws: on AdminClient timeout/exception, returns
     *   ClusterHealth(0, 0, "degraded").
     */
    ClusterHealth health();

    /**
     * Preconditions: none.
     * Postconditions: returns every known broker node and, for every
     *   partition of every known topic, its current leader broker id,
     *   replica set, and in-sync replica set. Never throws: on
     *   AdminClient timeout/exception, returns an empty ClusterMetadata
     *   (empty brokers list, empty partitionLeaders list).
     */
    ClusterMetadata metadata();

    /**
     * Preconditions: groupId is non-null.
     * Postconditions: returns one ConsumerGroupStat per known member of the
     *   consumer group identified by groupId, with assignedPartitions
     *   reflecting that member's current partition assignment count. Never
     *   throws: on AdminClient timeout/exception, returns an empty list.
     */
    List<ConsumerGroupStat> consumerStats(String groupId);
}
