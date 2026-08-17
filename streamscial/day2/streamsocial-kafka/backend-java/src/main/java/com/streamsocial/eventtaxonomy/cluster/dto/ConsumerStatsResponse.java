package com.streamsocial.eventtaxonomy.cluster.dto;

import java.util.List;

/**
 * Response body for {@code GET /consumers/stats}, per design.md's Data
 * Models section: {@code ConsumerStatsResponse(String groupId, List<ConsumerGroupStat> consumers)}.
 */
public record ConsumerStatsResponse(
        String groupId,
        List<ConsumerGroupStat> consumers) {
}
