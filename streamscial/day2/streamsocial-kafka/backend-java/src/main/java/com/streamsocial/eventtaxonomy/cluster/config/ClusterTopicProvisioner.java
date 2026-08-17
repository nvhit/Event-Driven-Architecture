package com.streamsocial.eventtaxonomy.cluster.config;

import org.apache.kafka.clients.admin.NewTopic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Idempotent topic provisioner for the cluster-scoped (3-broker) Kafka backend.
 *
 * <p>Implements the {@code ClusterTopicProvisioner} contract described in
 * design.md's "Component: ClusterProducerConfig" section
 * ({@code NewTopic ensureTopic(String topicName)}).
 *
 * <p>In a real deployment, {@code ensureTopic} would delegate to Kafka's
 * {@code AdminClient.createTopics}, which is a no-op/error (not a mutation)
 * when the topic already exists. This class models that same idempotency
 * contract at the object level using an in-memory registry of already
 * "provisioned" {@link NewTopic} definitions, so the behavior can be verified
 * without a live broker connection.
 *
 * <p>Preconditions: {@code topicName} is non-null/non-blank.
 * <p>Postconditions: the first call for a given {@code topicName} creates and
 * registers a new {@link NewTopic} with {@code partitions=9},
 * {@code replicationFactor=3}; every subsequent call for the same
 * {@code topicName} returns the SAME {@link NewTopic} instance/settings —
 * partition count and replication factor never change after first creation
 * (Property 6: Idempotent topic provisioning).
 */
public class ClusterTopicProvisioner {

    static final int STANDARD_PARTITIONS = 9;
    static final short STANDARD_REPLICATION_FACTOR = 3;

    private final Map<String, NewTopic> registeredTopics = new ConcurrentHashMap<>();

    /**
     * Preconditions: topicName is non-null/non-blank.
     * Postconditions: returns the existing NewTopic for topicName if one was
     *   already registered (partitions/replicationFactor unchanged), otherwise
     *   creates, registers, and returns a new NewTopic with
     *   partitions=9/replicationFactor=3.
     */
    public NewTopic ensureTopic(String topicName) {
        return registeredTopics.computeIfAbsent(
                topicName,
                name -> new NewTopic(name, STANDARD_PARTITIONS, STANDARD_REPLICATION_FACTOR));
    }
}
