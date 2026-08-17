package com.streamsocial.eventtaxonomy.cluster.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Plain unit tests for {@link ClusterProducerConfig}. No Spring context and no
 * running Kafka broker are required: the bean-producing methods are invoked
 * directly and the resulting objects are inspected.
 */
class ClusterProducerConfigTest {

    private final ClusterProducerConfig config = new ClusterProducerConfig();

    @Test
    void clusterProducerFactory_hasExpectedBootstrapServersAcksAndRetries() {
        ProducerFactory<String, Object> producerFactory = config.clusterProducerFactory();

        assertInstanceOf(DefaultKafkaProducerFactory.class, producerFactory);
        Map<String, Object> props =
                ((DefaultKafkaProducerFactory<String, Object>) producerFactory).getConfigurationProperties();

        assertEquals("localhost:9092,localhost:9093,localhost:9094",
                props.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("all", props.get(ProducerConfig.ACKS_CONFIG));
        assertEquals(10, props.get(ProducerConfig.RETRIES_CONFIG));
        assertEquals(StringSerializer.class, props.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(JsonSerializer.class, props.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
    }

    @Test
    void userActionTopic_hasNinePartitionsAndReplicationFactorThree() {
        NewTopic topic = config.userActionTopic();

        assertEquals("user_action", topic.name());
        assertEquals(9, topic.numPartitions());
        assertEquals((short) 3, topic.replicationFactor());
    }

    @Test
    void contentInteractionTopic_hasNinePartitionsAndReplicationFactorThree() {
        NewTopic topic = config.contentInteractionTopic();

        assertEquals("content_interaction", topic.name());
        assertEquals(9, topic.numPartitions());
        assertEquals((short) 3, topic.replicationFactor());
    }

    @Test
    void systemEventTopic_hasNinePartitionsAndReplicationFactorThree() {
        NewTopic topic = config.systemEventTopic();

        assertEquals("system_event", topic.name());
        assertEquals(9, topic.numPartitions());
        assertEquals((short) 3, topic.replicationFactor());
    }
}
