package com.streamsocial.eventtaxonomy.cluster.consumer;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Starts 3 named Kafka consumer instances (consumer-0/1/2) in the shared
 * {@code streamsocial-cluster-consumers} group on application startup, so
 * that partitions of {@code user_action}, {@code content_interaction}, and
 * {@code system_event} rebalance across them automatically.
 *
 * Per design.md's "Component: ClusterConsumerConfig + ConsumerBootstrap"
 * section.
 *
 * Preconditions: Spring context fully initialized (ApplicationReadyEvent
 *   fired), at least one broker reachable.
 * Postconditions: exactly 3 consumer containers (consumer-0, consumer-1,
 *   consumer-2) are started in group streamsocial-cluster-consumers;
 *   Kafka's group coordinator assigns/rebalances the 9 topic partitions
 *   across whichever of the 3 instances are alive.
 * Loop invariant: for i in [0,3), containers.size() == i+1 after i+1
 *   iterations; no two containers share a clientId.
 */
@Component
public class ConsumerBootstrap {

    private static final Logger log = LoggerFactory.getLogger(ConsumerBootstrap.class);

    private final ConsumerFactory<String, Object> consumerFactory;
    private final ConsumerStatsRegistry statsRegistry;
    private final List<KafkaMessageListenerContainer<String, Object>> containers = new ArrayList<>();

    public ConsumerBootstrap(ConsumerFactory<String, Object> clusterConsumerFactory,
                              ConsumerStatsRegistry statsRegistry) {
        this.consumerFactory = clusterConsumerFactory;
        this.statsRegistry = statsRegistry;
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void startConsumers() {
        log.info("Starting cluster consumers asynchronously...");
        for (int i = 0; i < 3; i++) {
            String clientId = "consumer-" + i;
            ContainerProperties containerProps =
                new ContainerProperties("user_action", "content_interaction", "system_event");
            containerProps.setGroupId("streamsocial-cluster-consumers");
            containerProps.setMessageListener((MessageListener<String, Object>) record -> {
                statsRegistry.recordConsumed(clientId, record.partition());
            });

            KafkaMessageListenerContainer<String, Object> container =
                new KafkaMessageListenerContainer<>(consumerFactory, containerProps);
            container.setBeanName(clientId);
            container.start();
            containers.add(container);

            // Stagger consumer starts to avoid rebalance storm
            if (i < 2) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    @PreDestroy
    public void stopConsumers() {
        containers.forEach(KafkaMessageListenerContainer::stop);
    }

    /**
     * Package-private accessor for tests to verify container count/naming
     * without requiring a live broker connection or a full Spring context.
     * Not part of the component's public API.
     */
    List<KafkaMessageListenerContainer<String, Object>> getContainers() {
        return containers;
    }
}
