package com.streamsocial.eventtaxonomy.cluster.controller;

import com.streamsocial.eventtaxonomy.cluster.dto.RecordMetadataResponse;
import com.streamsocial.eventtaxonomy.dto.UserRegistrationRequest;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Exposes {@code POST /events/user/register}, the build-verification
 * "test event" endpoint from design.md's "Component: REST controllers"
 * section (reference class {@code EventController} in
 * {@code com.streamsocial.eventtaxonomy.cluster.controller}).
 *
 * <p>Distinct from the existing single-broker
 * {@code com.streamsocial.eventtaxonomy.controller.EventController}
 * (Day 1, port 8080, {@code /api/v1/events/*}), which is untouched by this
 * feature. This controller publishes to the cluster {@code KafkaTemplate}
 * (acks=all, retries=10, 3 brokers) so build-verify flows can confirm the
 * cluster accepts writes and observe the resulting partition/offset
 * (Requirement 4.8).
 */
@RestController("clusterEventController")
@RequestMapping("/events")
public class EventController {

    private static final String TOPIC = "user_action";
    private static final long SEND_TIMEOUT_SECONDS = 10;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventController(KafkaTemplate<String, Object> clusterKafkaTemplate) {
        this.kafkaTemplate = clusterKafkaTemplate;
    }

    /**
     * Preconditions: request body deserializes into
     *   {@link UserRegistrationRequest} (username/email, may be null).
     * Postconditions: on success, sends {@code request} to the
     *   {@code user_action} topic (keyed by {@code username}, per the
     *   design's user-action topic taxonomy) via the cluster
     *   {@code KafkaTemplate}, blocks up to {@value #SEND_TIMEOUT_SECONDS}
     *   seconds for the broker ack, and returns 200 with a
     *   {@link RecordMetadataResponse} carrying the resulting partition
     *   and offset (Requirement 2.5, 4.8). If the send fails or times out
     *   (e.g. fewer than min.insync.replicas (2) reachable, after
     *   exhausting the producer's configured retries), returns 503
     *   Service Unavailable instead of letting the exception propagate as
     *   an unhandled 500 (Requirement 2.4, 4.8).
     */
    @PostMapping("/user/register")
    public ResponseEntity<RecordMetadataResponse> registerUser(
            @RequestBody UserRegistrationRequest request) {
        try {
            SendResult<String, Object> result = kafkaTemplate
                    .send(TOPIC, request.getUsername(), request)
                    .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            RecordMetadata metadata = result.getRecordMetadata();
            return ResponseEntity.ok(new RecordMetadataResponse(
                    true, metadata.topic(), metadata.partition(), metadata.offset()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return serviceUnavailable();
        } catch (ExecutionException | TimeoutException e) {
            return serviceUnavailable();
        }
    }

    private ResponseEntity<RecordMetadataResponse> serviceUnavailable() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new RecordMetadataResponse(false, TOPIC, -1, -1));
    }
}
