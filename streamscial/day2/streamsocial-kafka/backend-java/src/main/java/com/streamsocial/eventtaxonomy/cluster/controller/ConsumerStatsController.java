package com.streamsocial.eventtaxonomy.cluster.controller;

import com.streamsocial.eventtaxonomy.cluster.config.ClusterInspector;
import com.streamsocial.eventtaxonomy.cluster.dto.ConsumerStatsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes cluster consumer group stats as {@code GET /consumers/stats}, per
 * design.md's "Component: AdminClient-backed Cluster Introspection" section.
 *
 * <p>Delegates entirely to {@link ClusterInspector#consumerStats(String)},
 * which already wraps the {@code AdminClient}'s {@code
 * describeConsumerGroups()} call and the timeout/exception resilience
 * (never throws; falls back to an empty list). This controller is a thin
 * pass-through and holds no AdminClient-specific logic itself.
 */
@RestController
@RequestMapping("/consumers")
public class ConsumerStatsController {

    private static final String GROUP_ID = "streamsocial-cluster-consumers";

    private final ClusterInspector clusterInspector;

    public ConsumerStatsController(ClusterInspector clusterInspector) {
        this.clusterInspector = clusterInspector;
    }

    /**
     * Preconditions: none.
     * Postconditions: returns a {@link ConsumerStatsResponse} with
     *   groupId == GROUP_ID and consumers == {@link
     *   ClusterInspector#consumerStats(String)} verbatim, with a 200
     *   status. Never throws to the caller, since {@code consumerStats}
     *   itself never throws.
     */
    @GetMapping("/stats")
    public ConsumerStatsResponse stats() {
        return new ConsumerStatsResponse(GROUP_ID, clusterInspector.consumerStats(GROUP_ID));
    }
}
