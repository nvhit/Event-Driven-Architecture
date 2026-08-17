package com.streamsocial.eventtaxonomy.cluster.config;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;
import org.apache.kafka.clients.admin.NewTopic;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Property-based test for Property 6 (Idempotent topic provisioning), per
 * design.md's Correctness Properties section:
 *
 * "Re-running cluster startup against an already-provisioned cluster does not
 * change existing topic partition/replication settings (Kafka's
 * AdminClient.createTopics on an existing topic is a no-op/error, not a
 * mutation)."
 *
 * There is no live Kafka broker in this test environment, so the property is
 * exercised at the object/model level against {@link ClusterTopicProvisioner},
 * which maintains an in-memory registry simulating "already provisioned"
 * topics.
 *
 * <p><b>Validates: Requirements 1.1, 2.3</b>
 */
class ClusterTopicProvisionerPropertyTest {

    @Property
    void repeatedEnsureTopicCallsNeverChangePartitionOrReplicationSettings(
            @ForAll @AlphaChars @StringLength(min = 1, max = 40) String topicName,
            @ForAll @IntRange(min = 2, max = 20) int repetitions) {

        ClusterTopicProvisioner provisioner = new ClusterTopicProvisioner();

        NewTopic first = provisioner.ensureTopic(topicName);

        for (int i = 1; i < repetitions; i++) {
            NewTopic subsequent = provisioner.ensureTopic(topicName);

            assertEquals(first.numPartitions(), subsequent.numPartitions(),
                    "numPartitions must not change after first creation");
            assertEquals(first.replicationFactor(), subsequent.replicationFactor(),
                    "replicationFactor must not change after first creation");
        }
    }

    @Property
    void ensureTopicAlwaysCreatesStandardPartitionAndReplicationSettings(
            @ForAll @AlphaChars @StringLength(min = 1, max = 40) String topicName) {

        ClusterTopicProvisioner provisioner = new ClusterTopicProvisioner();

        NewTopic topic = provisioner.ensureTopic(topicName);

        assertEquals(9, topic.numPartitions());
        assertEquals((short) 3, topic.replicationFactor());
    }
}
