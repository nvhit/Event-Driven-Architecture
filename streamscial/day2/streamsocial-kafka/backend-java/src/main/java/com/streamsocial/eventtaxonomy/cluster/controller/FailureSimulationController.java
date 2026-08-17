package com.streamsocial.eventtaxonomy.cluster.controller;

import com.streamsocial.eventtaxonomy.cluster.dto.SimulateFailureRequest;
import com.streamsocial.eventtaxonomy.cluster.dto.SimulateFailureResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Exposes {@code POST /cluster/simulate-failure}, per design.md's
 * "Component: FailureSimulationController" section.
 *
 * <p>Security note (see design.md's Security Considerations): the
 * {@code broker_name} validation regex ({@code ^kafka-broker-[1-3]$}) also
 * serves as a command-injection guard for the {@link ProcessBuilder} call
 * below. Validation runs strictly before any process is spawned, and
 * {@link ProcessBuilder} is invoked with a String array (not a shell
 * string), so the input is never interpreted by a shell.
 */
@RestController
@RequestMapping("/cluster")
public class FailureSimulationController {

    private static final Pattern VALID_BROKER = Pattern.compile("^kafka-broker-[1-3]$");

    /**
     * Preconditions: none (request body may contain any string, including
     *   null, empty, or malicious {@code broker_name} values).
     * Postconditions: if {@code request.brokerName()} does not match
     *   {@code ^kafka-broker-[1-3]$}, returns 400 Bad Request and no
     *   process is spawned (Requirement 4.7). Otherwise, issues
     *   {@code docker stop <brokerName>} as a child process via the
     *   array-based {@link ProcessBuilder} constructor (never a shell
     *   string) and returns 202 Accepted with
     *   {@code SimulateFailureResponse("failure-simulated", brokerName)}
     *   (Requirement 4.6).
     */
    @PostMapping("/simulate-failure")
    public ResponseEntity<SimulateFailureResponse> simulateFailure(
            @RequestBody SimulateFailureRequest request) {
        String brokerName = request.brokerName();
        if (brokerName == null || !VALID_BROKER.matcher(brokerName).matches()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            stopBrokerContainer(brokerName);
        } catch (IOException e) {
            // docker CLI itself unavailable/unlaunchable; still acknowledge
            // the simulation request per design.md (fire-and-forget, does
            // not block on container shutdown or cluster reconvergence).
        }
        return ResponseEntity.accepted()
                .body(new SimulateFailureResponse("failure-simulated", brokerName));
    }

    /**
     * Spawns {@code docker stop <brokerName>} as a child process using the
     * array-based {@link ProcessBuilder} constructor (never a shell
     * string). Extracted from {@link #simulateFailure} into its own
     * protected method purely so tests can substitute a spy that verifies
     * this method is never invoked for invalid input (per design.md's
     * Unit Testing section), without changing runtime behavior.
     */
    protected void stopBrokerContainer(String brokerName) throws IOException {
        new ProcessBuilder("docker", "stop", brokerName).start();
    }
}
