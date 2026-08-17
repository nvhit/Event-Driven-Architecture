package com.streamsocial.eventtaxonomy.cluster.controller;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Focused MockMvc test for {@link EventController} (task 6.6).
 *
 * <p>Verifies Requirement 2.5/4.8 (a successful cluster send returns 200
 * with the resulting partition/offset) and Requirement 2.4/4.8 (a failed
 * send, e.g. after exhausting retries due to insufficient in-sync
 * replicas, returns 503 instead of an unhandled 500).
 */
@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KafkaTemplate<String, Object> clusterKafkaTemplate;

    @Test
    void registerUser_returns200WithPartitionAndOffset_whenSendSucceeds() throws Exception {
        RecordMetadata recordMetadata = new RecordMetadata(
                new TopicPartition("user_action", 4), 42L, 0, 0L, -1, -1);
        SendResult<String, Object> sendResult = new SendResult<>(null, recordMetadata);
        when(clusterKafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        mockMvc.perform(post("/events/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"clustertest\",\"email\":\"test@cluster.com\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"success\":true,\"topic\":\"user_action\",\"partition\":4,\"offset\":42}"));
    }

    @Test
    void registerUser_returns503_whenSendFails() throws Exception {
        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("insufficient in-sync replicas"));
        when(clusterKafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(failedFuture);

        mockMvc.perform(post("/events/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"clustertest\",\"email\":\"test@cluster.com\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().json(
                        "{\"success\":false,\"topic\":\"user_action\",\"partition\":-1,\"offset\":-1}"));
    }
}
