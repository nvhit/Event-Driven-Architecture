package com.streamsocial.eventtaxonomy.cluster.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * AdminClient configuration for the cluster-scoped (3-broker) Kafka backend.
 *
 * Bound to the 3-broker bootstrap-servers list (localhost:9092,9093,9094)
 * rather than the single-broker config in
 * {@code com.streamsocial.eventtaxonomy.config.KafkaConfig}. Backs cluster
 * introspection (health/metadata/consumer-stats) used by the REST controllers
 * in {@code com.streamsocial.eventtaxonomy.cluster.controller}.
 */
@Configuration
public class ClusterAdminConfig {

    private static final String BOOTSTRAP_SERVERS =
            "localhost:9092,localhost:9093,localhost:9094";

    /**
     * Preconditions: none (static bootstrap list, cluster may be partially up).
     * Postconditions: returns an AdminClient bound to the 3-broker bootstrap
     *   list. AdminClient implements Closeable, so Spring auto-detects and
     *   invokes close() on context shutdown without an explicit
     *   destroyMethod being declared.
     */
    @Bean
    public AdminClient clusterAdminClient() {
        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        return AdminClient.create(props);
    }
}
