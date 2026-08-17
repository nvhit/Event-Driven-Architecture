package com.streamsocial.eventtaxonomy.cluster.config;

import com.streamsocial.eventtaxonomy.cluster.dto.BrokerNode;
import com.streamsocial.eventtaxonomy.cluster.dto.ClusterHealth;
import com.streamsocial.eventtaxonomy.cluster.dto.ClusterMetadata;
import com.streamsocial.eventtaxonomy.cluster.dto.ConsumerGroupStat;
import com.streamsocial.eventtaxonomy.cluster.dto.PartitionLeaderInfo;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * {@link AdminClient}-backed implementation of {@link ClusterInspector}.
 *
 * <p>Backs {@code /cluster/health}, {@code /cluster/metadata}, and
 * {@code /consumers/stats} per design.md's "Component: AdminClient-backed
 * Cluster Introspection" section. Every AdminClient call is bounded by a
 * {@link #ADMIN_TIMEOUT_SECONDS}-second timeout and every public method
 * catches any exception (including timeouts) to return a degraded/empty
 * result instead of propagating, per the design's "Error Handling" section.
 */
@Component
public class AdminClientClusterInspector implements ClusterInspector {

    private static final long ADMIN_TIMEOUT_SECONDS = 5;

    private final AdminClient adminClient;

    public AdminClientClusterInspector(AdminClient clusterAdminClient) {
        this.adminClient = clusterAdminClient;
    }

    /**
     * Preconditions: none.
     * Postconditions: see {@link ClusterInspector#health()}.
     */
    @Override
    public ClusterHealth health() {
        try {
            DescribeClusterResult result = adminClient.describeCluster();
            int brokerCount = result.nodes().get(ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS).size();
            int topicCount = adminClient.listTopics().names()
                    .get(ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS).size();
            String status = brokerCount >= 2 ? "healthy" : "degraded";
            return new ClusterHealth(brokerCount, topicCount, status);
        } catch (Exception e) {
            return new ClusterHealth(0, 0, "degraded");
        }
    }

    /**
     * Preconditions: none.
     * Postconditions: see {@link ClusterInspector#metadata()}.
     */
    @Override
    public ClusterMetadata metadata() {
        try {
            DescribeClusterResult clusterResult = adminClient.describeCluster();
            Collection<Node> nodes = clusterResult.nodes().get(ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Node controller = clusterResult.controller().get(ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            int controllerId = controller != null ? controller.id() : -1;

            List<BrokerNode> brokers = nodes.stream()
                    .map(node -> new BrokerNode(node.id(), node.host(), node.port(), node.id() == controllerId))
                    .toList();

            Set<String> topicNames = adminClient.listTopics().names()
                    .get(ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Map<String, TopicDescription> topicDescriptions = adminClient
                    .describeTopics(topicNames)
                    .allTopicNames()
                    .get(ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            List<PartitionLeaderInfo> partitionLeaders = new ArrayList<>();
            for (TopicDescription description : topicDescriptions.values()) {
                for (TopicPartitionInfo partitionInfo : description.partitions()) {
                    partitionLeaders.add(new PartitionLeaderInfo(
                            description.name(),
                            partitionInfo.partition(),
                            partitionInfo.leader() != null ? partitionInfo.leader().id() : -1,
                            partitionInfo.replicas().stream().map(Node::id).toList(),
                            partitionInfo.isr().stream().map(Node::id).toList()));
                }
            }

            return new ClusterMetadata(brokers, partitionLeaders);
        } catch (Exception e) {
            return new ClusterMetadata(Collections.emptyList(), Collections.emptyList());
        }
    }

    /**
     * Preconditions: groupId is non-null.
     * Postconditions: see {@link ClusterInspector#consumerStats(String)}.
     *
     * <p>{@code totalLag} is always {@code 0L}: computing real consumer lag
     * requires combining {@code listConsumerGroupOffsets} with
     * {@code listOffsets} (log-end-offsets) per topic-partition, which is out
     * of scope for this lab implementation (matching the precedent set by
     * {@code ConsumerStatsRegistry}, which also defers lag to {@code 0L}).
     */
    @Override
    public List<ConsumerGroupStat> consumerStats(String groupId) {
        try {
            Map<String, ConsumerGroupDescription> descriptions = adminClient
                    .describeConsumerGroups(List.of(groupId))
                    .all()
                    .get(ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            ConsumerGroupDescription description = descriptions.get(groupId);
            if (description == null) {
                return Collections.emptyList();
            }

            Collection<MemberDescription> members = description.members();
            return members.stream()
                    .map(member -> new ConsumerGroupStat(
                            member.consumerId(),
                            member.assignment().topicPartitions().size(),
                            0L))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
