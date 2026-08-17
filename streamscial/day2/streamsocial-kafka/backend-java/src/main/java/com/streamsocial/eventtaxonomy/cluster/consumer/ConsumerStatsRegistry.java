package com.streamsocial.eventtaxonomy.cluster.consumer;

import com.streamsocial.eventtaxonomy.cluster.dto.ConsumerGroupStat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.LongAdder;

/**
 * In-memory, thread-safe registry tracking per-consumer-instance state for
 * the {@code streamsocial-cluster-consumers} group.
 *
 * <p>Per design.md's "Component: ClusterConsumerConfig + ConsumerBootstrap"
 * section, {@code ConsumerBootstrap}'s message listener calls
 * {@link #recordConsumed(String, int)} for every consumed record (on a Kafka
 * consumer thread), while partition assignment can additionally be pushed
 * explicitly (e.g. from a rebalance listener) via
 * {@link #updateAssignment(String, Set)}. The {@code /consumers/stats}
 * endpoint reads a consistent {@link #snapshot()} from an HTTP thread, so all
 * internal state uses concurrent collections.
 *
 * <p>{@code totalLag} in the returned {@link ConsumerGroupStat} entries is
 * always {@code 0L}: this component has no live broker/AdminClient
 * connection to compute real consumer lag. Real lag is computed later by the
 * AdminClient-backed {@code ClusterInspector} (task 5).
 */
@Component
public class ConsumerStatsRegistry {

    private final ConcurrentHashMap<String, Set<Integer>> assignedPartitionsByConsumer =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LongAdder> consumedCountByConsumer =
            new ConcurrentHashMap<>();

    /**
     * Preconditions: consumerId is non-null; partition is a valid partition id.
     * Postconditions: consumerId is recorded as having consumed one more
     *   record (its consumed-record counter is incremented by 1) and
     *   consumerId's assigned-partition set is guaranteed to contain
     *   partition (added if not already present). Safe to call concurrently
     *   from multiple Kafka consumer threads for different consumerIds.
     */
    public void recordConsumed(String consumerId, int partition) {
        assignedPartitionsByConsumer
                .computeIfAbsent(consumerId, id -> new ConcurrentSkipListSet<>())
                .add(partition);
        consumedCountByConsumer
                .computeIfAbsent(consumerId, id -> new LongAdder())
                .increment();
    }

    /**
     * Preconditions: consumerId is non-null; partitions is non-null (may be empty).
     * Postconditions: consumerId's assigned-partition set is replaced with a
     *   snapshot copy of partitions; assignedPartitions count for consumerId
     *   in the next snapshot() equals partitions.size(). Consumed-record
     *   counts for consumerId are unaffected. Intended to be called from a
     *   Kafka rebalance listener whenever partition ownership changes.
     */
    public void updateAssignment(String consumerId, Set<Integer> partitions) {
        assignedPartitionsByConsumer.put(consumerId, new ConcurrentSkipListSet<>(partitions));
        consumedCountByConsumer.computeIfAbsent(consumerId, id -> new LongAdder());
    }

    /**
     * Preconditions: none.
     * Postconditions: returns one ConsumerGroupStat per consumerId known to
     *   this registry (via recordConsumed and/or updateAssignment calls so
     *   far), with assignedPartitions equal to the current size of that
     *   consumer's assigned-partition set and totalLag always 0L. Does not
     *   mutate internal state; safe to call concurrently with writers
     *   (reflects a point-in-time, not necessarily atomic across consumers,
     *   view).
     */
    public List<ConsumerGroupStat> snapshot() {
        Set<String> knownConsumerIds = new ConcurrentSkipListSet<>();
        knownConsumerIds.addAll(assignedPartitionsByConsumer.keySet());
        knownConsumerIds.addAll(consumedCountByConsumer.keySet());

        return knownConsumerIds.stream()
                .map(consumerId -> new ConsumerGroupStat(
                        consumerId,
                        assignedPartitionsByConsumer
                                .getOrDefault(consumerId, Set.of())
                                .size(),
                        0L))
                .toList();
    }
}
