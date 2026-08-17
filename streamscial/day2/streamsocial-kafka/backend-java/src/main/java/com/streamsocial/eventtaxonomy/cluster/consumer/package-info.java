/**
 * Cluster-scoped consumer bootstrap and stats tracking.
 *
 * Holds {@code ConsumerBootstrap} (starts consumer-0/1/2 containers on
 * ApplicationReadyEvent within the streamsocial-cluster-consumers group) and
 * {@code ConsumerStatsRegistry} (in-memory per-consumer assigned-partition
 * and consumed-record tracking used by {@code /consumers/stats}).
 */
package com.streamsocial.eventtaxonomy.cluster.consumer;
