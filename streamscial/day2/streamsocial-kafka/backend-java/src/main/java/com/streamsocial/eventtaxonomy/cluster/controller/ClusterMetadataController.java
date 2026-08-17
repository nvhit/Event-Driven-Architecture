package com.streamsocial.eventtaxonomy.cluster.controller;

import com.streamsocial.eventtaxonomy.cluster.config.ClusterInspector;
import com.streamsocial.eventtaxonomy.cluster.dto.ClusterMetadata;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes cluster topology as {@code GET /cluster/metadata}, per design.md's
 * "Component: AdminClient-backed Cluster Introspection" section.
 *
 * <p>Delegates entirely to {@link ClusterInspector}, which already wraps the
 * {@code AdminClient} with the {@code describeCluster()}/{@code
 * describeTopics()} calls and the timeout/exception resilience (never
 * throws; falls back to an empty {@code ClusterMetadata}). This controller
 * is a thin pass-through and holds no AdminClient-specific logic itself.
 */
@RestController
@RequestMapping("/cluster")
public class ClusterMetadataController {

    private final ClusterInspector clusterInspector;

    public ClusterMetadataController(ClusterInspector clusterInspector) {
        this.clusterInspector = clusterInspector;
    }

    /**
     * Preconditions: none.
     * Postconditions: returns {@link ClusterInspector#metadata()} verbatim
     *   as the response body with a 200 status, including every known
     *   broker node and, for each partition of each known topic, its
     *   leader broker id, replica set, and in-sync replica set. Never
     *   throws to the caller, since {@link ClusterInspector#metadata()}
     *   itself never throws.
     */
    @GetMapping("/metadata")
    public ClusterMetadata metadata() {
        return clusterInspector.metadata();
    }
}
