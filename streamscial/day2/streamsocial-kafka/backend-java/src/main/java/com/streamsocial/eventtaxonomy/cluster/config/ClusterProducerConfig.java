package com.streamsocial.eventtaxonomy.cluster.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Producer-side configuration for the cluster-scoped (3-broker) Kafka backend.
 *
 * Bound to the 3-broker bootstrap-servers list (localhost:9092,9093,9094)
 * rather than the single-broker config in
 * {@code com.streamsocial.eventtaxonomy.config.KafkaConfig}.
 */
@Configuration
public class ClusterProducerConfig {

    private static final String BOOTSTRAP_SERVERS =
            "localhost:9092,localhost:9093,localhost:9094";

    /**
     * Preconditions: none (static bootstrap list, cluster may be partially up).
     * Postconditions: returns a ProducerFactory whose producer config sets
     *   acks=all and retries=10, guaranteeing at-least-once delivery as long
     *   as >= min.insync.replicas (2) brokers are reachable.
     */
    @Bean
    public ProducerFactory<String, Object> clusterProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 10);
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * KafkaAdmin configured to NOT block application startup if brokers are
     * unavailable. Topics will be created on the first successful connection.
     */
    @Bean
    public KafkaAdmin clusterKafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configs.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);
        configs.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 10000);
        KafkaAdmin admin = new KafkaAdmin(configs);
        admin.setFatalIfBrokerNotAvailable(false);
        admin.setAutoCreate(true);
        return admin;
    }

    @Bean
    public KafkaTemplate<String, Object> clusterKafkaTemplate(
            ProducerFactory<String, Object> clusterProducerFactory) {
        return new KafkaTemplate<>(clusterProducerFactory);
    }

    /**
     * Preconditions: topicName is non-blank.
     * Postconditions: registers a NewTopic bean with 9 partitions and
     *   replication factor 3; Kafka's AdminClient auto-provisions it on
     *   context startup via KafkaAdmin.
     */
    @Bean
    public NewTopic userActionTopic() {
        return new NewTopic("user_action", 9, (short) 3);
    }

    @Bean
    public NewTopic contentInteractionTopic() {
        return new NewTopic("content_interaction", 9, (short) 3);
    }

    @Bean
    public NewTopic systemEventTopic() {
        return new NewTopic("system_event", 9, (short) 3);
    }
}
