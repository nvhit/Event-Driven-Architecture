package com.streamsocial.eventtaxonomy.config;

import com.streamsocial.eventtaxonomy.events.BaseEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka configuration - only active with profile "kafka"
 * 
 * Start with Kafka: mvn spring-boot:run -Dspring-boot.run.profiles=kafka
 * Start without Kafka: mvn spring-boot:run (default, in-memory EventBus only)
 */
@Configuration
@Profile("kafka")
public class KafkaConfig {

    @Bean
    public NewTopic streamsocialEventsTopic() {
        return TopicBuilder.name("streamsocial-events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
