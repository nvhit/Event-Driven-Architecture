package com.streamsocial.eventtaxonomy.cluster.controller;

import com.streamsocial.eventtaxonomy.cluster.config.ClusterInspector;
import com.streamsocial.eventtaxonomy.cluster.dto.ClusterHealth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Focused MockMvc test for {@link ClusterHealthController} (task 6.1).
 *
 * <p>Verifies the controller is a thin pass-through: {@code GET
 * /cluster/health} returns 200 with the exact JSON body produced by the
 * mocked {@link ClusterInspector#health()}, without exercising any real
 * AdminClient logic.
 */
@WebMvcTest(ClusterHealthController.class)
class ClusterHealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClusterInspector clusterInspector;

    @Test
    void health_returns200WithClusterInspectorResult() throws Exception {
        when(clusterInspector.health()).thenReturn(new ClusterHealth(3, 3, "healthy"));

        mockMvc.perform(get("/cluster/health"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"brokerCount\":3,\"topicCount\":3,\"status\":\"healthy\"}"));
    }

    @Test
    void health_returnsDegradedZeroState_whenClusterInspectorReportsDegraded() throws Exception {
        when(clusterInspector.health()).thenReturn(new ClusterHealth(0, 0, "degraded"));

        mockMvc.perform(get("/cluster/health"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"brokerCount\":0,\"topicCount\":0,\"status\":\"degraded\"}"));
    }
}
