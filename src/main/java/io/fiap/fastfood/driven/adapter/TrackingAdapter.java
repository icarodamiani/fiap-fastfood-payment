package io.fiap.fastfood.driven.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fiap.fastfood.driven.core.domain.model.Tracking;
import io.fiap.fastfood.driven.core.domain.tracking.port.outbound.TrackingPort;
import io.fiap.fastfood.driven.core.messaging.MessagingPort;
import io.vavr.CheckedFunction1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
public class TrackingAdapter implements TrackingPort {

    private final MessagingPort messagingPort;
    private final ObjectMapper objectMapper;
    private final String queue;

    public TrackingAdapter(MessagingPort messagingPort,
                           ObjectMapper objectMapper,
                           @Value("${aws.sqs.tracking.queue}") String queue) {
        this.messagingPort = messagingPort;
        this.objectMapper = objectMapper;
        this.queue = queue;
    }

    public Mono<SendMessageResponse> create(Tracking tracking) {
        return messagingPort.send(queue, tracking, serializePayload());
    }

    private <T> CheckedFunction1<T, String> serializePayload() {
        return objectMapper::writeValueAsString;
    }
}
