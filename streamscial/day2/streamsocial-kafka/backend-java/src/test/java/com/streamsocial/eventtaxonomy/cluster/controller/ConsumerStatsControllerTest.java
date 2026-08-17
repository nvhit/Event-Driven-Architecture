package com.streamsocial.eventtaxonomy.cluster.controller;

import com.streamsocial.eventtaxonomy.cluster.config.ClusterInspector;
import com.streamsocial.eventtaxonomy.cluster.dto.ConsumerGroupStat;
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
 * Focused MockMvc test for {@link ConsumerStatsController} (task 6.3).
 *
 * <p>Verifies the controller is a thin pass-through: {@code GET
 * /consumers/stats} returns 200 with a body wrapping the exact list
 * produced by the mocked {@link ClusterInspector#consumerStats(String)}
 * under the fixed {@code streamsocial-cluster-consumers} group id, without
 * exercising any real AdminClient logic.
 */
@WebMvcTest(ConsumerStatsController.class)
class ConsumerStatsControllerTest {

    private static final String GROUP_ID = "streamsocial-cluster-consumers";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClusterInspector clusterInspector;

    @Test
    void stats_returns200WithClusterInspectorResult() throws Exception {
        List<ConsumerGroupStat> sample = List.of(
                new ConsumerGroupStat("consumer-0", 3, 0L),
                new ConsumerGroupStat("consumer-1", 2, 0L),
                new ConsumerGroupStat("consumer-2", 4, 0L));

        when(clusterInspector.consumerStats(GROUP_ID)).thenReturn(sample);

        mockMvc.perform(get("/consumers/stats"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "groupId": "streamsocial-cluster-consumers",
                          "consumers": [
                            {"consumerId":"consumer-0","assignedPartitions":3,"totalLag":0},
                            {"consumerId":"consumer-1","assignedPartitions":2,"totalLag":0},
                            {"consumerId":"consumer-2","assignedPartitions":4,"totalLag":0}
                          ]
                        }
                        """));
    }

    @Test
    void stats_returnsEmptyConsumers_whenClusterInspectorReportsEmpty() throws Exception {
        when(clusterInspector.consumerStats(GROUP_ID)).thenReturn(List.of());

        mockMvc.perform(get("/consumers/stats"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"groupId\":\"streamsocial-cluster-consumers\",\"consumers\":[]}"));
    }
}
