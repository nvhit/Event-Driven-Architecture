package com.streamsocial.eventtaxonomy.cluster.controller;

import com.streamsocial.eventtaxonomy.cluster.config.ClusterInspector;
import com.streamsocial.eventtaxonomy.cluster.dto.BrokerNode;
import com.streamsocial.eventtaxonomy.cluster.dto.ClusterMetadata;
import com.streamsocial.eventtaxonomy.cluster.dto.PartitionLeaderInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Focused MockMvc test for {@link ClusterMetadataController} (task 6.2).
 *
 * <p>Verifies the controller is a thin pass-through: {@code GET
 * /cluster/metadata} returns 200 with the exact JSON body produced by the
 * mocked {@link ClusterInspector#metadata()}, without exercising any real
 * AdminClient logic.
 */
@WebMvcTest(ClusterMetadataController.class)
class ClusterMetadataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClusterInspector clusterInspector;

    @Test
    void metadata_returns200WithClusterInspectorResult() throws Exception {
        ClusterMetadata sample = new ClusterMetadata(
                List.of(
                        new BrokerNode(1, "localhost", 9092, true),
                        new BrokerNode(2, "localhost", 9093, false)),
                List.of(
                        new PartitionLeaderInfo("user_action", 0, 1, List.of(1, 2, 3), List.of(1, 2, 3)),
                        new PartitionLeaderInfo("user_action", 1, 2, List.of(1, 2, 3), List.of(1, 2))));

        when(clusterInspector.metadata()).thenReturn(sample);

        mockMvc.perform(get("/cluster/metadata"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "brokers": [
                            {"id":1,"host":"localhost","port":9092,"isController":true},
                            {"id":2,"host":"localhost","port":9093,"isController":false}
                          ],
                          "partitionLeaders": [
                            {"topic":"user_action","partition":0,"leaderBrokerId":1,"replicaBrokerIds":[1,2,3],"inSyncReplicaBrokerIds":[1,2,3]},
                            {"topic":"user_action","partition":1,"leaderBrokerId":2,"replicaBrokerIds":[1,2,3],"inSyncReplicaBrokerIds":[1,2]}
                          ]
                        }
                        """));
    }

    @Test
    void metadata_returnsEmptyState_whenClusterInspectorReportsEmpty() throws Exception {
        when(clusterInspector.metadata()).thenReturn(new ClusterMetadata(List.of(), List.of()));

        mockMvc.perform(get("/cluster/metadata"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"brokers\":[],\"partitionLeaders\":[]}"));
    }
}
