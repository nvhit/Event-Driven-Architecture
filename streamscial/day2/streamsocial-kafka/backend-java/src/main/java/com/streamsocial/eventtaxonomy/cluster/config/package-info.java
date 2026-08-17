/**
 * Configuration beans for the cluster-scoped (3-broker) Kafka backend.
 *
 * Holds {@code ClusterProducerConfig} (ProducerFactory/KafkaTemplate + NewTopic
 * provisioning) and {@code ClusterConsumerConfig} (ConsumerFactory), each bound
 * to the 3-broker bootstrap-servers list (localhost:9092,9093,9094) rather than
 * the single-broker config in {@code com.streamsocial.eventtaxonomy.config}.
 */
package com.streamsocial.eventtaxonomy.cluster.config;
