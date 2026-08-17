package com.streamsocial.eventtaxonomy.cluster.controller;

import com.streamsocial.eventtaxonomy.cluster.config.ClusterInspector;
import com.streamsocial.eventtaxonomy.cluster.dto.ClusterHealth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes cluster-wide health as {@code GET /cluster/health}, per design.md's
 * "Component: AdminClient-backed Cluster Introspection" section.
 *
 * <p>Delegates entirely to {@link ClusterInspector}, which already wraps the
 * {@code AdminClient} with the {@code describeCluster()}/{@code listTopics()}
 * calls and the timeout/exception resilience (never throws; falls back to
 * {@code ClusterHealth(0, 0, "degraded")}). This controller is a thin
 * pass-through and holds no AdminClient-specific logic itself.
 */
@RestController
@RequestMapping("/cluster")
public class ClusterHealthController {

    private final ClusterInspector clusterInspector;

    public ClusterHealthController(ClusterInspector clusterInspector) {
        this.clusterInspector = clusterInspector;
    }

    /**
     * Preconditions: none.
     * Postconditions: returns {@link ClusterInspector#health()} verbatim as
     *   the response body with a 200 status. Never throws to the caller,
     *   since {@link ClusterInspector#health()} itself never throws.
     */
    @GetMapping("/health")
    public ClusterHealth health() {
        return clusterInspector.health();
    }
}
