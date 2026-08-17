package com.streamsocial.eventtaxonomy.cluster.consumer;

import com.streamsocial.eventtaxonomy.cluster.dto.ConsumerGroupStat;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Plain unit tests for {@link ConsumerStatsRegistry}. No Spring context and
 * no running Kafka broker are required: the registry is exercised directly
 * in memory.
 */
class ConsumerStatsRegistryTest {

    private final ConsumerStatsRegistry registry = new ConsumerStatsRegistry();

    @Test
    void recordConsumed_incrementsCountForCorrectConsumerOnly() {
        registry.recordConsumed("consumer-0", 0);
        registry.recordConsumed("consumer-0", 1);
        registry.recordConsumed("consumer-0", 0);
        registry.recordConsumed("consumer-1", 2);

        List<ConsumerGroupStat> snapshot = registry.snapshot();

        ConsumerGroupStat consumer0 = findStat(snapshot, "consumer-0");
        ConsumerGroupStat consumer1 = findStat(snapshot, "consumer-1");

        // recordConsumed also tracks the partition as assigned to that consumer.
        assertEquals(2, consumer0.assignedPartitions()); // partitions {0, 1}
        assertEquals(1, consumer1.assignedPartitions()); // partition {2}
    }

    @Test
    void updateAssignment_setsAssignedPartitionCountCorrectly() {
        registry.updateAssignment("consumer-0", Set.of(0, 1, 2));
        registry.updateAssignment("consumer-1", Set.of(3));

        List<ConsumerGroupStat> snapshot = registry.snapshot();

        assertEquals(3, findStat(snapshot, "consumer-0").assignedPartitions());
        assertEquals(1, findStat(snapshot, "consumer-1").assignedPartitions());
    }

    @Test
    void updateAssignment_replacesPreviousAssignmentRatherThanMerging() {
        registry.updateAssignment("consumer-0", Set.of(0, 1, 2));
        registry.updateAssignment("consumer-0", Set.of(5));

        List<ConsumerGroupStat> snapshot = registry.snapshot();

        assertEquals(1, findStat(snapshot, "consumer-0").assignedPartitions());
    }

    @Test
    void snapshot_returnsCorrectEntriesForMultipleConsumers() {
        registry.updateAssignment("consumer-0", Set.of(0, 1, 2));
        registry.updateAssignment("consumer-1", Set.of(3, 4, 5));
        registry.updateAssignment("consumer-2", Set.of(6, 7, 8));
        registry.recordConsumed("consumer-0", 0);
        registry.recordConsumed("consumer-1", 3);

        List<ConsumerGroupStat> snapshot = registry.snapshot();

        assertEquals(3, snapshot.size());
        assertEquals(9, snapshot.stream().mapToInt(ConsumerGroupStat::assignedPartitions).sum());
        assertTrue(snapshot.stream().allMatch(stat -> stat.totalLag() == 0L));
    }

    @Test
    void snapshot_isEmptyWhenNoConsumerActivityRecorded() {
        assertTrue(registry.snapshot().isEmpty());
    }

    private static ConsumerGroupStat findStat(List<ConsumerGroupStat> snapshot, String consumerId) {
        return snapshot.stream()
                .filter(stat -> stat.consumerId().equals(consumerId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No stat found for " + consumerId));
    }
}
