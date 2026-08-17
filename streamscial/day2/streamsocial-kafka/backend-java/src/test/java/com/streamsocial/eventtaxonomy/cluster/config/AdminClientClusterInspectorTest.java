package com.streamsocial.eventtaxonomy.cluster.config;

import com.streamsocial.eventtaxonomy.cluster.dto.ClusterHealth;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.common.Node;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.common.internals.KafkaFutureImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Focused sanity-check unit tests for {@link AdminClientClusterInspector}
 * (task 5.2). Structural verification only: a basic healthy case and a
 * failure/timeout case. Exhaustive brokerCount=0,1,2,3 matrix (task 5.3) and
 * genuine future-timeout coverage (task 5.5) are included below.
 */
class AdminClientClusterInspectorTest {

    @Test
    void health_returnsBrokerCountTopicCountAndHealthyStatus_forThreeBrokerCluster() throws Exception {
        AdminClient adminClient = mock(AdminClient.class);
        DescribeClusterResult describeClusterResult = mock(DescribeClusterResult.class);

        KafkaFutureImpl<Collection<Node>> nodesFuture = new KafkaFutureImpl<>();
        nodesFuture.complete(List.of(
                new Node(1, "localhost", 9092),
                new Node(2, "localhost", 9093),
                new Node(3, "localhost", 9094)));
        when(describeClusterResult.nodes()).thenReturn(nodesFuture);
        when(adminClient.describeCluster()).thenReturn(describeClusterResult);

        ListTopicsResult listTopicsResult = mock(ListTopicsResult.class);
        KafkaFutureImpl<Set<String>> namesFuture = new KafkaFutureImpl<>();
        namesFuture.complete(Set.of("user_action", "content_interaction", "system_event"));
        when(listTopicsResult.names()).thenReturn(namesFuture);
        when(adminClient.listTopics()).thenReturn(listTopicsResult);

        AdminClientClusterInspector inspector = new AdminClientClusterInspector(adminClient);

        ClusterHealth health = inspector.health();

        assertEquals(3, health.brokerCount());
        assertEquals(3, health.topicCount());
        assertEquals("healthy", health.status());
    }

    @Test
    void health_returnsDegradedZeroState_whenDescribeClusterThrows() {
        AdminClient adminClient = mock(AdminClient.class);
        when(adminClient.describeCluster()).thenThrow(new org.apache.kafka.common.KafkaException(
                new TimeoutException("simulated AdminClient timeout")));

        AdminClientClusterInspector inspector = new AdminClientClusterInspector(adminClient);

        ClusterHealth health = inspector.health();

        assertEquals(0, health.brokerCount());
        assertEquals(0, health.topicCount());
        assertEquals("degraded", health.status());
    }

    /**
     * Task 5.5: genuine {@code .get(timeout, unit)} timeout coverage,
     * distinct from {@link #health_returnsDegradedZeroState_whenDescribeClusterThrows()}
     * (which simulates {@code describeCluster()} throwing synchronously
     * before any future is even returned). Here {@code describeCluster()}
     * returns normally, but the {@code nodes()} future is intentionally left
     * incomplete, so the blocking {@code .get(5, TimeUnit.SECONDS)} call
     * inside {@link AdminClientClusterInspector#health()} genuinely times
     * out and throws {@link TimeoutException}, which must be caught and
     * translated into the degraded/zero-state result per Requirement 4.3.
     *
     * <p>This test intentionally blocks for the real ~5s
     * {@code ADMIN_TIMEOUT_SECONDS} window since that constant is currently
     * hardcoded (not injectable) in production code. It is deliberately not
     * parameterized/repeated so it only adds this one-time cost to the suite.
     */
    @Test
    void health_returnsDegradedZeroState_whenNodesFutureGenuinelyTimesOut() {
        AdminClient adminClient = mock(AdminClient.class);
        DescribeClusterResult describeClusterResult = mock(DescribeClusterResult.class);

        // Never completed: .get(timeout, unit) on this future will block for
        // the real 5-second ADMIN_TIMEOUT_SECONDS window and then throw
        // TimeoutException, exercising the genuine timeout path.
        KafkaFutureImpl<Collection<Node>> neverCompletingNodesFuture = new KafkaFutureImpl<>();
        when(describeClusterResult.nodes()).thenReturn(neverCompletingNodesFuture);
        when(adminClient.describeCluster()).thenReturn(describeClusterResult);

        AdminClientClusterInspector inspector = new AdminClientClusterInspector(adminClient);

        ClusterHealth health = inspector.health();

        assertEquals(0, health.brokerCount());
        assertEquals(0, health.topicCount());
        assertEquals("degraded", health.status());
    }

    /**
     * Task 5.3: exhaustive matrix of brokerCount = 0, 1, 2, 3 verifying the
     * status derivation rule from Requirement 4.2 (healthy iff brokerCount
     * >= 2). topicCount is fixed at a small non-zero set since it is not the
     * focus of this test.
     */
    @ParameterizedTest(name = "brokerCount={0} -> status={1}")
    @CsvSource({
            "0, degraded",
            "1, degraded",
            "2, healthy",
            "3, healthy"
    })
    void health_derivesStatus_fromBrokerCountMatrix(int brokerCount, String expectedStatus) throws Exception {
        AdminClient adminClient = mock(AdminClient.class);
        DescribeClusterResult describeClusterResult = mock(DescribeClusterResult.class);

        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < brokerCount; i++) {
            nodes.add(new Node(i + 1, "localhost", 9092 + i));
        }
        KafkaFutureImpl<Collection<Node>> nodesFuture = new KafkaFutureImpl<>();
        nodesFuture.complete(nodes);
        when(describeClusterResult.nodes()).thenReturn(nodesFuture);
        when(adminClient.describeCluster()).thenReturn(describeClusterResult);

        ListTopicsResult listTopicsResult = mock(ListTopicsResult.class);
        KafkaFutureImpl<Set<String>> namesFuture = new KafkaFutureImpl<>();
        namesFuture.complete(Set.of("user_action", "content_interaction"));
        when(listTopicsResult.names()).thenReturn(namesFuture);
        when(adminClient.listTopics()).thenReturn(listTopicsResult);

        AdminClientClusterInspector inspector = new AdminClientClusterInspector(adminClient);

        ClusterHealth health = inspector.health();

        assertEquals(brokerCount, health.brokerCount());
        assertEquals(expectedStatus, health.status());
    }
}
