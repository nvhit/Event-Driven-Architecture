package com.streamsocial.eventtaxonomy.cluster.consumer;

import com.streamsocial.eventtaxonomy.cluster.dto.ConsumerGroupStat;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Property-based test for Property 4 (Rebalance completeness), per
 * design.md's Correctness Properties section:
 *
 * "At all times, SUM(consumer_i.assignedPartitions) == total partition
 * count across subscribed topics for whichever consumer instances are
 * currently alive (no partition is left unassigned while >=1 group member
 * is alive)."
 *
 * There is no live Kafka broker in this test environment, so real
 * rebalancing (Kafka's group coordinator) cannot be exercised end-to-end.
 * This property is instead exercised at the model/simulation level: a
 * deterministic round-robin {@link PartitionRebalanceSimulator} stands in
 * for Kafka's real rebalance protocol, and its resulting per-consumer
 * partition assignment is pushed into the production
 * {@link ConsumerStatsRegistry} via {@code updateAssignment(...)}, then read
 * back via {@code snapshot()} - tying the property to real registry
 * bookkeeping rather than to the simulator's own arithmetic in isolation.
 *
 * <p><b>Validates: Requirements 3.3, 3.4, 6.3</b>
 */
class ConsumerRebalanceCompletenessPropertyTest {

    private static final List<String> ALL_CONSUMER_IDS =
            List.of("consumer-0", "consumer-1", "consumer-2");

    @Property
    void sumOfAssignedPartitionsAcrossAliveConsumersEqualsTotalPartitionCount(
            @ForAll @IntRange(min = 0, max = 100) int totalPartitionCount,
            @ForAll("aliveConsumerSubsets") List<String> aliveConsumerIds) {

        Map<String, Set<Integer>> simulatedAssignment =
                PartitionRebalanceSimulator.assign(totalPartitionCount, aliveConsumerIds);

        ConsumerStatsRegistry registry = new ConsumerStatsRegistry();
        for (String consumerId : aliveConsumerIds) {
            registry.updateAssignment(consumerId, simulatedAssignment.get(consumerId));
        }

        int sumOfAssignedPartitions = registry.snapshot().stream()
                .mapToInt(ConsumerGroupStat::assignedPartitions)
                .sum();

        assertEquals(totalPartitionCount, sumOfAssignedPartitions,
                "sum of assigned partitions across alive consumers must equal "
                        + "the total subscribed partition count");
    }

    /**
     * Generates non-empty subsets of size 1-3 of {consumer-0, consumer-1,
     * consumer-2}, representing which of the 3 consumer instances are
     * currently "alive".
     */
    @Provide
    Arbitrary<List<String>> aliveConsumerSubsets() {
        return Arbitraries.of(ALL_CONSUMER_IDS)
                .list()
                .ofMinSize(1)
                .ofMaxSize(ALL_CONSUMER_IDS.size())
                .uniqueElements();
    }
}
