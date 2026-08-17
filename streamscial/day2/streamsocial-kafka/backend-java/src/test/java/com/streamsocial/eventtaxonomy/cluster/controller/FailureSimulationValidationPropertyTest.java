package com.streamsocial.eventtaxonomy.cluster.controller;

import com.streamsocial.eventtaxonomy.cluster.dto.SimulateFailureRequest;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.Tuple;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Property-based test for design.md's "Property 5: Failure simulation
 * validation" (task 6.5):
 *
 * <p>"POST /cluster/simulate-failure only executes docker stop when
 * broker_name matches kafka-broker-[1-3]; invalid names never reach the
 * process-spawning step."
 *
 * <p>Validates: Requirements 4.6, 4.7
 *
 * <p>{@link FailureSimulationController#stopBrokerContainer} was extracted
 * as a small, behavior-preserving refactor (same regex validation, same
 * {@code ProcessBuilder} call, just isolated into its own method) purely
 * so this test can spy on the controller and assert the process launcher
 * is invoked if-and-only-if {@code broker_name} matches
 * {@code ^kafka-broker-[1-3]$}, per design.md's Unit Testing guidance
 * ("verify via a spy/mock process-launcher abstraction rather than a real
 * process"). The spy's {@code stopBrokerContainer} is stubbed to do
 * nothing, so no real {@code docker}/subprocess execution ever occurs
 * during this test, keeping it fast and hermetic.
 */
class FailureSimulationValidationPropertyTest {

    private static final Pattern VALID_BROKER = Pattern.compile("^kafka-broker-[1-3]$");

    /**
     * Mixes ~90% arbitrary garbage strings (including empty, long,
     * whitespace, and shell-metacharacter-laden strings) with the 3 exact
     * valid broker names, so the property test exercises both the
     * "always rejected before execution" case and the "valid input
     * triggers exactly one launcher invocation" case.
     */
    @Provide
    Arbitrary<String> brokerNames() {
        Arbitrary<String> garbage = Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .withChars('-', '_', ' ', ';', '&', '|', '`', '$', '.', '/')
                .ofMinLength(0)
                .ofMaxLength(40);
        Arbitrary<String> validNames = Arbitraries.of(
                "kafka-broker-1", "kafka-broker-2", "kafka-broker-3");
        return Arbitraries.frequencyOf(
                Tuple.of(1, validNames),
                Tuple.of(9, garbage));
    }

    @Property(tries = 500)
    void processLauncherIsInvokedIfAndOnlyIfBrokerNameMatchesValidPattern(
            @ForAll("brokerNames") String brokerName) throws IOException {
        FailureSimulationController spyController = spy(new FailureSimulationController());
        doNothing().when(spyController).stopBrokerContainer(any());

        ResponseEntity<?> response =
                spyController.simulateFailure(new SimulateFailureRequest(brokerName));

        boolean matchesValidPattern = VALID_BROKER.matcher(brokerName).matches();

        if (matchesValidPattern) {
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
            verify(spyController, times(1)).stopBrokerContainer(brokerName);
        } else {
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            verify(spyController, never()).stopBrokerContainer(any());
        }
    }

    @Property(tries = 500)
    void response400IffBrokerNameDoesNotMatchValidPattern(
            @ForAll("brokerNames") String brokerName) throws IOException {
        FailureSimulationController spyController = spy(new FailureSimulationController());
        doNothing().when(spyController).stopBrokerContainer(any());

        ResponseEntity<?> response =
                spyController.simulateFailure(new SimulateFailureRequest(brokerName));

        boolean matchesValidPattern = VALID_BROKER.matcher(brokerName).matches();
        boolean is400 = response.getStatusCode() == HttpStatus.BAD_REQUEST;

        assertThat(is400).isEqualTo(!matchesValidPattern);
    }
}
