package io.fiap.fastfood.driven.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fiap.fastfood.driven.core.domain.model.Notification;
import io.fiap.fastfood.driven.core.domain.notification.port.outbound.NotificationPort;
import io.fiap.fastfood.driven.core.messaging.MessagingPort;
import io.vavr.CheckedFunction1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class NotificationAdapter implements NotificationPort {

    private final MessagingPort messagingPort;
    private final ObjectMapper objectMapper;
    private final String queue;

    public NotificationAdapter(MessagingPort messagingPort,
                               ObjectMapper objectMapper,
                               @Value("${aws.sqs.notification.queue}") String queue) {
        this.messagingPort = messagingPort;
        this.objectMapper = objectMapper;
        this.queue = queue;
    }

    public Mono<Void> sendNotification(Notification notification) {
        return messagingPort.send(queue, notification, serializePayload());
    }

    private <T> CheckedFunction1<T, String> serializePayload() {
        return objectMapper::writeValueAsString;
    }
}
