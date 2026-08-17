package com.streamsocial.eventtaxonomy.cluster.consumer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Test-only helper that models Kafka's group-coordinator rebalance behavior
 * at a simplified level: every partition of the subscribed topics is
 * deterministically assigned to exactly one currently-"alive" consumer
 * instance (round-robin by partition index), and no partition is ever left
 * unassigned as long as at least one consumer instance is alive.
 *
 * <p>This does not talk to a real Kafka broker/coordinator - it exists only
 * to give {@code ConsumerRebalanceCompletenessPropertyTest} a concrete,
 * deterministic "assignment" to feed into the production
 * {@link ConsumerStatsRegistry} so the property can be checked against real
 * registry bookkeeping rather than against fabricated arithmetic.
 */
final class PartitionRebalanceSimulator {

    private PartitionRebalanceSimulator() {
    }

    /**
     * Preconditions: totalPartitionCount >= 0; aliveConsumerIds is non-null
     *   and non-empty.
     * Postconditions: returns a map from each id in aliveConsumerIds to the
     *   set of partition indices (0 until totalPartitionCount) assigned to
     *   it via round-robin (partition p -> aliveConsumerIds.get(p %
     *   aliveConsumerIds.size())); every partition index in
     *   [0, totalPartitionCount) appears in exactly one consumer's set, and
     *   the union of all returned sets has size == totalPartitionCount.
     */
    static Map<String, Set<Integer>> assign(int totalPartitionCount, List<String> aliveConsumerIds) {
        if (aliveConsumerIds == null || aliveConsumerIds.isEmpty()) {
            throw new IllegalArgumentException("aliveConsumerIds must be non-empty");
        }

        Map<String, Set<Integer>> byConsumer = aliveConsumerIds.stream()
                .collect(Collectors.toMap(id -> id, id -> new TreeSet<Integer>()));

        for (int partition = 0; partition < totalPartitionCount; partition++) {
            String owner = aliveConsumerIds.get(partition % aliveConsumerIds.size());
            byConsumer.get(owner).add(partition);
        }

        return byConsumer;
    }
}
