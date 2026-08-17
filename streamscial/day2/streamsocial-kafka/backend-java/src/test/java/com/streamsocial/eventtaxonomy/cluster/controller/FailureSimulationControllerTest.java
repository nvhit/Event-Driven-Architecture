package com.streamsocial.eventtaxonomy.cluster.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Focused MockMvc test for {@link FailureSimulationController} (task 6.4).
 *
 * <p>Verifies Requirement 4.6 (valid {@code broker_name} triggers 202 with
 * {@code status: "failure-simulated"}) and Requirement 4.7 (any
 * {@code broker_name} not matching {@code ^kafka-broker-[1-3]$}, including
 * command-injection attempts, is rejected with 400 before any process is
 * spawned). This controller has no injectable process-launching seam (it
 * calls {@code new ProcessBuilder(...).start()} inline per design.md's
 * reference), so the valid-broker-name case genuinely invokes
 * {@code docker stop kafka-broker-2} as a subprocess; since that container
 * generally is not running in the test environment, the Docker CLI itself
 * exits with a harmless "no such container" non-zero status rather than
 * throwing a Java exception, so the controller still returns 202.
 */
@WebMvcTest(FailureSimulationController.class)
class FailureSimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void simulateFailure_returns202_whenBrokerNameIsValid() throws Exception {
        mockMvc.perform(post("/cluster/simulate-failure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"broker_name\":\"kafka-broker-2\"}"))
                .andExpect(status().isAccepted())
                .andExpect(content().json(
                        "{\"status\":\"failure-simulated\",\"broker\":\"kafka-broker-2\"}"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "kafka-broker-4",
            "kafka-broker-0",
            "kafka-broker-1; rm -rf /",
            "kafka-broker-1 && echo pwned",
            "kafka-broker-1`echo pwned`",
            "kafka-broker-1|echo pwned",
            "not-a-broker",
            "KAFKA-BROKER-1",
            "kafka-broker-1 ",
            ""
    })
    void simulateFailure_returns400_whenBrokerNameIsInvalid(String brokerName) throws Exception {
        String body = "{\"broker_name\":" + toJsonStringLiteral(brokerName) + "}";

        mockMvc.perform(post("/cluster/simulate-failure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void simulateFailure_returns400_whenBrokerNameIsNull() throws Exception {
        mockMvc.perform(post("/cluster/simulate-failure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"broker_name\":null}"))
                .andExpect(status().isBadRequest());
    }

    private static String toJsonStringLiteral(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
