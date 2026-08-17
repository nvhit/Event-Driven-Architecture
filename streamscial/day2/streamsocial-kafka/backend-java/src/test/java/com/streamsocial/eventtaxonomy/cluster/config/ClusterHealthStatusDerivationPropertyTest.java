package com.streamsocial.eventtaxonomy.cluster.config;

import com.streamsocial.eventtaxonomy.cluster.dto.ClusterHealth;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.internals.KafkaFutureImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Property-based test for Property 3 (Health status derivation), per
 * design.md's Correctness Properties section:
 *
 * "ClusterHealth.status == "healthy" &#8660; brokerCount &gt;= 2. This is a
 * pure function of brokerCount, never independently settable."
 *
 * <p>For any generated {@code brokerCount} in [0, 10], this test mocks
 * {@link AdminClient} (reusing the same Mockito + {@link KafkaFutureImpl}
 * pattern as {@link AdminClientClusterInspectorTest}) to return exactly
 * {@code brokerCount} {@link Node} instances from
 * {@code describeCluster().nodes()} and a fixed small topic set from
 * {@code listTopics().names()}, then asserts the iff relationship directly
 * as a single boolean-equality assertion rather than two separate branches,
 * which is a strictly stronger check of the biconditional than an
 * exhaustive matrix over a small fixed set of values.
 *
 * <p><b>Validates: Requirements 4.2, 4.3</b>
 */
class ClusterHealthStatusDerivationPropertyTest {

    @Property
    void statusIsHealthyIffBrokerCountIsAtLeastTwo(
            @ForAll @IntRange(min = 0, max = 10) int brokerCount) throws Exception {

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
        assertEquals(health.status().equals("healthy"), brokerCount >= 2,
                "status must be \"healthy\" if and only if brokerCount >= 2");
    }
}
