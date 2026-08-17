package com.streamsocial.eventtaxonomy.cluster.consumer;

import com.streamsocial.eventtaxonomy.cluster.config.ClusterConsumerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Plain unit test for {@link ConsumerBootstrap}. No live Kafka broker is
 * required: {@code KafkaMessageListenerContainer#start()} creates its
 * background consumer thread and returns immediately, so the container's
 * (name, count) can be verified synchronously right after
 * {@link ConsumerBootstrap#startConsumers()} returns. Connection-retry noise
 * from the background thread (there is no broker listening on
 * localhost:9092-9094 in this test environment) happens asynchronously and
 * does not affect this test's assertions; {@link #tearDown()} stops the
 * containers promptly afterwards so no threads leak into other tests.
 */
class ConsumerBootstrapTest {

    private final ConsumerFactory<String, Object> consumerFactory =
            new ClusterConsumerConfig().clusterConsumerFactory();
    private final ConsumerStatsRegistry statsRegistry = new ConsumerStatsRegistry();
    private final ConsumerBootstrap bootstrap = new ConsumerBootstrap(consumerFactory, statsRegistry);

    @AfterEach
    void tearDown() {
        bootstrap.stopConsumers();
    }

    @Test
    void startConsumers_createsExactlyThreeContainersWithDistinctBeanNames() {
        bootstrap.startConsumers();

        List<KafkaMessageListenerContainer<String, Object>> containers = bootstrap.getContainers();

        assertEquals(3, containers.size());

        Set<String> beanNames = containers.stream()
                .map(KafkaMessageListenerContainer::getBeanName)
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(3, beanNames.size(), "bean names must be distinct");
        assertEquals(Set.of("consumer-0", "consumer-1", "consumer-2"), beanNames);
    }
}
