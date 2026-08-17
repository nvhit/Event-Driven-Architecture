package com.streamsocial.eventtaxonomy.cluster;

import com.streamsocial.eventtaxonomy.cluster.config.ClusterInspector;
import com.streamsocial.eventtaxonomy.cluster.controller.ClusterHealthController;
import com.streamsocial.eventtaxonomy.cluster.controller.ClusterMetadataController;
import com.streamsocial.eventtaxonomy.cluster.controller.ConsumerStatsController;
import com.streamsocial.eventtaxonomy.cluster.controller.EventController;
import com.streamsocial.eventtaxonomy.cluster.controller.FailureSimulationController;
import com.streamsocial.eventtaxonomy.cluster.dto.BrokerNode;
import com.streamsocial.eventtaxonomy.cluster.dto.ClusterHealth;
import com.streamsocial.eventtaxonomy.cluster.dto.ClusterMetadata;
import com.streamsocial.eventtaxonomy.cluster.dto.ConsumerGroupStat;
import com.streamsocial.eventtaxonomy.cluster.dto.ConsumerStatsResponse;
import com.streamsocial.eventtaxonomy.cluster.dto.PartitionLeaderInfo;
import com.streamsocial.eventtaxonomy.cluster.dto.RecordMetadataResponse;
import com.streamsocial.eventtaxonomy.cluster.dto.SimulateFailureResponse;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Broader integration-style test (task 6.7) exercising all 4 cluster REST
 * controllers plus {@code EventController} together, through real HTTP
 * calls against an embedded servlet container, as opposed to the narrow
 * per-controller {@code @WebMvcTest} tests (tasks 6.1-6.6) which each boot
 * only a single controller slice.
 *
 * <p><b>Spring context strategy (and why):</b> this test intentionally does
 * <em>not</em> activate the literal {@code "cluster"} Spring profile and does
 * <em>not</em> boot the full {@code EventTaxonomyApplication}. Two concrete
 * problems were found and avoided by scoping the context narrowly instead:
 * <ul>
 *   <li><b>Port 8000 hardcoding</b>: {@code application-cluster.properties}
 *       sets {@code server.port=8000} unconditionally. With
 *       {@code WebEnvironment.RANDOM_PORT}, Spring Boot's test infrastructure
 *       overrides {@code server.port} to a random free port <em>after</em>
 *       property sources are resolved, so in practice this does not conflict
 *       - but relying on that override every test run (in parallel/CI
 *       environments where port 8000 might already be bound by a real
 *       running instance of this very backend) is an avoidable risk for no
 *       benefit, since the port-8000 binding itself is already exercised by
 *       {@code application-cluster.properties} (task 2) via the classpath
 *       property assertion below.</li>
 *   <li><b>Real AdminClient/KafkaAdmin startup calls</b>: activating the
 *       {@code "cluster"} profile sets {@code spring.autoconfigure.exclude=}
 *       (blank), which <em>re-enables</em> Spring Boot's
 *       {@code KafkaAutoConfiguration} (excluded by default in
 *       {@code application.properties}). That autoconfiguration creates its
 *       <em>own</em> {@code KafkaAdmin} bean bound to
 *       {@code spring.kafka.bootstrap-servers}, entirely separate from this
 *       project's manually-defined {@code clusterAdminClient} bean - so
 *       {@code @MockBean AdminClient} would <em>not</em> intercept it. On
 *       context startup, that autoconfigured {@code KafkaAdmin} scans for
 *       {@code NewTopic} beans (there are 3, from {@code ClusterProducerConfig})
 *       and attempts to create them against the real (unreachable in this
 *       test) 3-broker bootstrap list, which is slow and non-hermetic even
 *       though {@code spring.kafka.admin.fail-fast} defaults to {@code false}
 *       (so it would not fail the build, just add real network-timeout
 *       delay). Additionally, {@code ConsumerBootstrap}'s
 *       {@code @EventListener(ApplicationReadyEvent.class)} would start 3
 *       real {@code KafkaMessageListenerContainer}s against the same
 *       unreachable brokers. Keeping the default profile (Kafka
 *       autoconfiguration excluded) and registering only the 5 controllers
 *       under test - plus {@code @MockBean}s for their direct collaborators
 *       ({@code ClusterInspector}, cluster {@code KafkaTemplate}) - avoids
 *       both issues entirely while still exercising the full web
 *       layer + dependency wiring + real HTTP dispatch for each endpoint.</li>
 * </ul>
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {
                ClusterIntegrationTest.MinimalWebTestConfig.class,
                ClusterHealthController.class,
                ClusterMetadataController.class,
                ConsumerStatsController.class,
                FailureSimulationController.class,
                EventController.class
        })
class ClusterIntegrationTest {

    private static final String GROUP_ID = "streamsocial-cluster-consumers";

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ClusterInspector clusterInspector;

    @MockBean
    private KafkaTemplate<String, Object> clusterKafkaTemplate;

    /**
     * Minimal {@code @Configuration} carrying {@code @EnableAutoConfiguration}
     * so the embedded servlet container, Jackson message converters, and MVC
     * dispatcher machinery are wired up - without pulling in the full
     * {@code EventTaxonomyApplication} (which would trigger the
     * component-scan-driven wiring described in the class Javadoc above). No
     * {@code @ComponentScan} is declared, so only the controller classes
     * explicitly listed in {@code @SpringBootTest(classes = ...)} are
     * registered as beans.
     */
    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    static class MinimalWebTestConfig {
    }

    // ---- Requirement 4.9: port 8000 binding (documented pragmatic check) ----

    /**
     * Requirement 4.9 ("reachable at port 8000") is verified here by asserting
     * the {@code application-cluster.properties} file itself declares
     * {@code server.port=8000}, rather than by binding a live embedded server
     * to port 8000 in this test. {@code WebEnvironment.RANDOM_PORT} is used
     * for the actual HTTP calls below for test practicality/hermeticity (see
     * class Javadoc); the literal port-8000 binding is a one-line property
     * declaration already covered structurally by task 2, so re-verifying it
     * via live port binding here would add flakiness risk (e.g. if port 8000
     * is already in use on the test machine) without added confidence.
     */
    @Test
    void applicationClusterProperties_declaresPort8000() throws IOException {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("application-cluster.properties")) {
            assertNotNull(in, "application-cluster.properties must be on the test classpath");
            props.load(in);
        }
        assertEquals("8000", props.getProperty("server.port"));
    }

    // ---- GET /cluster/health ----

    @Test
    void clusterHealth_returns200WithInspectorResult() {
        when(clusterInspector.health()).thenReturn(new ClusterHealth(3, 3, "healthy"));

        ResponseEntity<ClusterHealth> response =
                restTemplate.getForEntity("/cluster/health", ClusterHealth.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ClusterHealth body = response.getBody();
        assertNotNull(body);
        assertEquals(3, body.brokerCount());
        assertEquals(3, body.topicCount());
        assertEquals("healthy", body.status());
    }

    // ---- GET /cluster/metadata ----

    @Test
    void clusterMetadata_returns200WithInspectorResult() {
        ClusterMetadata sample = new ClusterMetadata(
                List.of(
                        new BrokerNode(1, "localhost", 9092, true),
                        new BrokerNode(2, "localhost", 9093, false),
                        new BrokerNode(3, "localhost", 9094, false)),
                List.of(new PartitionLeaderInfo(
                        "user_action", 0, 1, List.of(1, 2, 3), List.of(1, 2, 3))));
        when(clusterInspector.metadata()).thenReturn(sample);

        ResponseEntity<ClusterMetadata> response =
                restTemplate.getForEntity("/cluster/metadata", ClusterMetadata.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ClusterMetadata body = response.getBody();
        assertNotNull(body);
        assertEquals(3, body.brokers().size());
        assertEquals(1, body.partitionLeaders().size());
        assertEquals("user_action", body.partitionLeaders().get(0).topic());
    }

    // ---- GET /consumers/stats ----

    @Test
    void consumerStats_returns200WithInspectorResult() {
        List<ConsumerGroupStat> sample = List.of(
                new ConsumerGroupStat("consumer-0", 3, 0L),
                new ConsumerGroupStat("consumer-1", 3, 0L),
                new ConsumerGroupStat("consumer-2", 3, 0L));
        when(clusterInspector.consumerStats(GROUP_ID)).thenReturn(sample);

        ResponseEntity<ConsumerStatsResponse> response =
                restTemplate.getForEntity("/consumers/stats", ConsumerStatsResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ConsumerStatsResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(GROUP_ID, body.groupId());
        assertEquals(3, body.consumers().size());
        assertEquals(9, body.consumers().stream().mapToInt(ConsumerGroupStat::assignedPartitions).sum());
    }

    // ---- POST /cluster/simulate-failure ----

    @Test
    void simulateFailure_returns202_whenBrokerNameIsValid() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{\"broker_name\":\"kafka-broker-2\"}", headers);

        ResponseEntity<SimulateFailureResponse> response = restTemplate.postForEntity(
                "/cluster/simulate-failure", request, SimulateFailureResponse.class);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        SimulateFailureResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("failure-simulated", body.status());
        assertEquals("kafka-broker-2", body.broker());
    }

    @Test
    void simulateFailure_returns400_whenBrokerNameIsInvalid() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{\"broker_name\":\"not-a-broker\"}", headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/cluster/simulate-failure", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ---- POST /events/user/register ----

    @Test
    void registerUser_returns200WithPartitionAndOffset_whenSendSucceeds() {
        RecordMetadata recordMetadata = new RecordMetadata(
                new TopicPartition("user_action", 4), 42L, 0, 0L, -1, -1);
        SendResult<String, Object> sendResult = new SendResult<>(null, recordMetadata);
        when(clusterKafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                "{\"username\":\"clustertest\",\"email\":\"test@cluster.com\"}", headers);

        ResponseEntity<RecordMetadataResponse> response = restTemplate.postForEntity(
                "/events/user/register", request, RecordMetadataResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        RecordMetadataResponse body = response.getBody();
        assertNotNull(body);
        assertTrue(body.success());
        assertEquals("user_action", body.topic());
        assertEquals(4, body.partition());
        assertEquals(42L, body.offset());
    }
}
