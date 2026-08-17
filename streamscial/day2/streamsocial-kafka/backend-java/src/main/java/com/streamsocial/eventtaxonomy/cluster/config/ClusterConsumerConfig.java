package com.streamsocial.eventtaxonomy.cluster.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Consumer-side configuration for the cluster-scoped (3-broker) Kafka backend.
 *
 * Bound to the 3-broker bootstrap-servers list (localhost:9092,9093,9094)
 * rather than the single-broker config in
 * {@code com.streamsocial.eventtaxonomy.config.KafkaConfig}.
 */
@Configuration
@EnableKafka
@EnableAsync
public class ClusterConsumerConfig {

    private static final String BOOTSTRAP_SERVERS =
            "localhost:9092,localhost:9093,localhost:9094";
    private static final String GROUP_ID = "streamsocial-cluster-consumers";

    /**
     * Preconditions: none (static bootstrap list, cluster may be partially up).
     * Postconditions: returns a ConsumerFactory whose consumer config sets
     *   group-id=streamsocial-cluster-consumers, auto-offset-reset=latest and
     *   enable-auto-commit=true, so consumers joining the shared group only
     *   see new records and rely on Kafka's periodic auto-commit.
     */
    @Bean
    public ConsumerFactory<String, Object> clusterConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        // Tuning for faster group coordination in dev environment
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);       // 10s (default 45s)
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);     // 3s (default 3s, must be < session timeout / 3)
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 30000);     // 30s (default 5min)
        props.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, 5000);          // refresh metadata every 5s

        return new DefaultKafkaConsumerFactory<>(props);
    }
}
