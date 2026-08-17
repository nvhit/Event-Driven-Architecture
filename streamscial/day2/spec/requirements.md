# Requirements Document

## Feature: Kafka Cluster (Multi-Broker Upgrade)

## Introduction

This feature evolves the existing StreamSocial Day 1 single-broker Kafka setup (`day2/streamsocial-kafka/`) into a production-style 3-broker Kafka cluster with replication, consumer group scaling, cluster monitoring APIs, a frontend observability dashboard, and fault-tolerance testing support. It is derived from the technical design in `design.md` and reflects an additive extension of the existing Spring Boot backend (`com.streamsocial.eventtaxonomy`) and Vite/React frontend — the existing single-broker demo and its `start.sh`/`stop.sh`/`verify.sh` scripts remain unmodified. Port 8000 is treated as the authoritative backend API port for this feature (distinct from the existing single-broker backend on port 8080), per the design's resolution of the slide deck's port 3001/8000 inconsistency.

## Requirements

### Requirement 1: Multi-Broker Docker Cluster

**User Story:** As a platform engineer, I want a 3-broker Kafka cluster coordinated by Zookeeper, so that the messaging layer tolerates a single broker failure without losing availability.

#### Acceptance Criteria

1. WHEN the cluster is started via `docker-compose -f docker-compose-cluster.yml up -d` THEN the system SHALL provision one Zookeeper node and three Kafka broker containers named `kafka-broker-1`, `kafka-broker-2`, and `kafka-broker-3`.
2. WHEN each broker container starts THEN the system SHALL assign `KAFKA_BROKER_ID` values 1, 2, and 3 respectively, matching the container name suffix.
3. WHEN each broker container starts THEN the system SHALL set `KAFKA_DEFAULT_REPLICATION_FACTOR=3` and `KAFKA_MIN_INSYNC_REPLICAS=2`.
4. WHEN each broker container starts THEN the system SHALL expose an advertised listener reachable at `localhost:9092`, `localhost:9093`, or `localhost:9094` respectively.
5. WHEN each broker container starts THEN the system SHALL apply `num.network.threads=8`, `num.io.threads=16`, `socket.send.buffer.bytes=102400`, and `socket.receive.buffer.bytes=102400`.
6. WHEN the cluster startup script runs THEN the system SHALL wait approximately 45 seconds after `docker-compose up -d` before proceeding, to allow broker registration and leader election to stabilize.
7. WHEN Kafdrop is started as part of the cluster THEN the system SHALL configure it to connect to all three brokers so all topics/partitions are visible.
8. IF fewer than 3 broker containers are defined in `docker-compose-cluster.yml` THEN the configuration SHALL be considered incomplete (the compose file MUST define exactly 3 kafka broker services plus zookeeper and kafdrop).

### Requirement 2: Cluster-Aware Producer

**User Story:** As a backend developer, I want a Spring Kafka producer configured against all 3 brokers with strong durability settings, so that published events survive a single broker failure.

#### Acceptance Criteria

1. WHEN the cluster producer bean is created THEN the system SHALL configure `bootstrap-servers` as `localhost:9092,localhost:9093,localhost:9094`.
2. WHEN the cluster producer bean is created THEN the system SHALL set `acks=all` and `retries=10`.
3. WHEN a new topic is provisioned by the application THEN the system SHALL create it with 9 partitions and replication factor 3 (equivalent to `new NewTopic(name, 9, (short) 3)`).
4. WHEN a producer send is attempted and fewer than `min.insync.replicas` (2) replicas are available for the target partition after exhausting retries THEN the system SHALL surface a failure response (HTTP 503) rather than silently dropping the record.
5. WHEN a producer send succeeds THEN the system SHALL return the resulting partition and offset to the caller.

### Requirement 3: Consumer Group Scaling

**User Story:** As a backend developer, I want multiple consumer instances running in the same consumer group, so that I can observe dynamic partition rebalancing across the cluster.

#### Acceptance Criteria

1. WHEN the cluster consumer configuration is created THEN the system SHALL configure `bootstrap-servers` as the same 3 brokers, `group-id=streamsocial-cluster-consumers`, `auto-offset-reset=latest`, and `enable-auto-commit=true`.
2. WHEN the Spring application publishes `ApplicationReadyEvent` THEN the system SHALL start exactly 3 consumer instances (`consumer-0`, `consumer-1`, `consumer-2`) in the `streamsocial-cluster-consumers` group.
3. WHEN all 3 consumer instances are running and healthy THEN the system SHALL ensure every subscribed topic partition is assigned to exactly one active consumer instance.
4. WHEN one consumer instance stops (e.g., process shutdown) THEN the system SHALL rebalance that instance's assigned partitions across the remaining active instances without manual intervention.
5. WHEN an operator runs `kafka-consumer-groups --describe --group streamsocial-cluster-consumers` THEN the output SHALL reflect the current partition assignment across the active consumer instances.

### Requirement 4: Cluster Monitoring APIs

**User Story:** As an operator, I want REST endpoints exposing cluster health, topology, and consumer lag, so that I can observe cluster state without using CLI tools directly.

#### Acceptance Criteria

1. WHEN a client sends `GET /cluster/health` THEN the system SHALL use `AdminClient.describeCluster()` to report `brokerCount`, `topicCount`, and a `status` field.
2. WHEN `brokerCount >= 2` THEN the system SHALL report `status="healthy"`; WHEN `brokerCount < 2` THEN the system SHALL report `status="degraded"`.
3. IF `describeCluster()` fails or times out THEN the system SHALL return `status="degraded"` with `brokerCount=0` rather than raising an unhandled error to the client.
4. WHEN a client sends `GET /cluster/metadata` THEN the system SHALL report the current broker topology, including each known broker node and, for each partition of each known topic, its leader broker id and in-sync replica set.
5. WHEN a client sends `GET /consumers/stats` THEN the system SHALL report per-consumer-instance metrics for the `streamsocial-cluster-consumers` group, including assigned partition count and lag.
6. WHEN a client sends `POST /cluster/simulate-failure` with a JSON body `{ "broker_name": "kafka-broker-N" }` where N is 1, 2, or 3 THEN the system SHALL trigger a simulated failure (e.g., `docker stop` on the named container) and return HTTP 202 with the broker name.
7. IF `POST /cluster/simulate-failure` is called with a `broker_name` that does not match `kafka-broker-1`, `kafka-broker-2`, or `kafka-broker-3` THEN the system SHALL return HTTP 400 and SHALL NOT execute any container-stopping operation.
8. WHEN a client sends `POST /events/user/register` with a valid registration payload THEN the system SHALL publish a test event to the cluster and return the resulting partition/offset, for use in build-verification flows.
9. WHEN the backend for this feature is running THEN it SHALL be reachable at port 8000 for all endpoints listed in this requirement.

### Requirement 5: Frontend Cluster Dashboard

**User Story:** As an operator, I want a dashboard showing broker health, consumer load, and partition leadership, plus controls to simulate broker failures, so that I can visually verify cluster behavior.

#### Acceptance Criteria

1. WHEN the dashboard loads THEN the system SHALL render a Broker Status Grid showing the real-time health of each of the 3 brokers, sourced from `/cluster/health` and `/cluster/metadata`.
2. WHEN the dashboard loads THEN the system SHALL render a Consumer Load Chart visualizing event/partition distribution across consumer instances, sourced from `/consumers/stats`.
3. WHEN the dashboard loads THEN the system SHALL render a Partition Leadership Monitor visualizing which broker currently leads each topic partition, sourced from `/cluster/metadata`.
4. WHEN the dashboard loads THEN the system SHALL render Fault Tolerance Controls providing a button per broker to trigger a simulated failure via `POST /cluster/simulate-failure`.
5. WHEN an operator clicks a Fault Tolerance Control button THEN the system SHALL send the corresponding `broker_name` in the POST request body and SHALL reflect the resulting degraded state in the Broker Status Grid on the next poll cycle.
6. WHEN the dashboard is polling cluster endpoints THEN the system SHALL continue rendering the last known state rather than crashing if a poll request fails.

### Requirement 6: Fault Tolerance Testing Workflow

**User Story:** As a platform engineer, I want a documented and repeatable fault-tolerance test procedure, so that I can verify the cluster survives a single broker outage and recovers correctly.

#### Acceptance Criteria

1. WHEN `docker stop kafka-broker-2` is executed while the cluster is healthy THEN the system SHALL continue accepting produce and consume traffic using the remaining brokers (`kafka-broker-1` and `kafka-broker-3`).
2. WHEN a broker is stopped THEN the system SHALL complete leader election for the affected partitions within 30 seconds.
3. WHEN a consumer instance is added to or removed from the `streamsocial-cluster-consumers` group during the test THEN the system SHALL rebalance partition assignments across the remaining/new set of active consumers.
4. WHEN an operator runs `kafka-topics --describe --topic streamsocial-events-clustered` THEN the output SHALL reflect the current replica and leader state for that topic.
5. WHEN an operator runs `kafka-consumer-groups --describe --group streamsocial-cluster-consumers` THEN the output SHALL reflect the current member assignment for the consumer group.
6. WHEN the stopped broker is restarted THEN the system SHALL rejoin it to the cluster and `GET /cluster/health` SHALL eventually report `brokerCount=3` again.

### Requirement 7: Operational Scripts

**User Story:** As a developer, I want start/stop scripts for the cluster analogous to the existing single-broker scripts, so that I can bring the cluster environment up and down consistently.

#### Acceptance Criteria

1. WHEN `start_cluster.sh` is executed THEN the system SHALL run `docker-compose -f docker-compose-cluster.yml up -d`, wait for stabilization, and start the cluster-scoped backend on port 8000.
2. WHEN `start_cluster.sh` completes THEN the system SHALL support verification via `curl http://localhost:8000/cluster/health`, a `POST` to `/events/user/register`, and `curl http://localhost:8000/consumers/stats`.
3. WHEN `stop_cluster.sh` is executed THEN the system SHALL stop the cluster-scoped backend process and run `docker-compose -f docker-compose-cluster.yml down`.
4. WHEN `start_cluster.sh`/`stop_cluster.sh` are run THEN the system SHALL NOT affect the existing single-broker `docker-compose.yml` stack or its `start.sh`/`stop.sh` scripts.

### Requirement 8: Non-Functional Performance Targets

**User Story:** As a platform engineer, I want the cluster to meet defined throughput, latency, availability, and recovery targets, so that it represents a realistic production-style deployment.

#### Acceptance Criteria

1. WHEN the cluster is under nominal load THEN the system SHALL be capable of sustaining greater than 10,000 events/sec cluster-wide.
2. WHEN a producer sends a record with `acks=all` under nominal conditions THEN the acknowledgment latency SHALL be less than 10ms.
3. WHEN a single broker fails THEN the cluster SHALL remain available for produce/consume operations, consistent with a 99.9% availability target.
4. WHEN a broker failure triggers leader election THEN recovery SHALL complete in under 30 seconds.

### Requirement 9: Out of Scope — 5-Broker Rack-Aware Cluster

**User Story:** As a course participant, I want the core 3-broker cluster feature clearly separated from the optional rack-aware stretch exercise, so that I understand what is required versus optional.

#### Acceptance Criteria

1. The system SHALL implement a 3-broker cluster with replication factor 3 as the core, required scope of this feature.
2. The system SHALL NOT require a 5-broker cluster, rack-aware replica placement, `broker.rack` configuration, or `--replica-assignment` usage as part of the core acceptance criteria.
3. WHERE documentation references the 5-broker rack-aware scenario THEN it SHALL be presented as a "Future Enhancements / Out of Scope" stretch exercise, not a core deliverable.

## Glossary

- **Broker**: A single Kafka server process (`kafka-broker-1/2/3`) that stores partition data and serves produce/consume requests.
- **Replication Factor (RF)**: The number of broker copies maintained for each partition (3 in this design).
- **In-Sync Replica (ISR)**: A replica that is fully caught up with the partition leader; `min.insync.replicas=2` means at least 2 ISRs must acknowledge a write for `acks=all` to succeed.
- **Consumer Group**: A named set of consumer instances (`streamsocial-cluster-consumers`) that jointly consume a topic's partitions, with Kafka rebalancing partition ownership as members join/leave.
- **Leader Election**: The process by which a new partition leader is chosen among remaining in-sync replicas after the previous leader's broker becomes unavailable.
- **AdminClient**: The Kafka client API used by the backend to introspect cluster state (`describeCluster`, `describeTopics`, `describeConsumerGroups`) without producing/consuming data.
- **Fault Tolerance Test**: The documented procedure of stopping a broker container and verifying the cluster continues serving traffic and recovers within target time bounds.
