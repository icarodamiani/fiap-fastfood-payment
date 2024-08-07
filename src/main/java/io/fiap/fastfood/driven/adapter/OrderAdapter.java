package io.fiap.fastfood.driven.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fiap.fastfood.driven.core.domain.model.Order;
import io.fiap.fastfood.driven.core.domain.order.port.outbound.OrderPort;
import io.fiap.fastfood.driven.core.messaging.MessagingPort;
import io.vavr.CheckedFunction1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
public class OrderAdapter implements OrderPort {

    private final MessagingPort messagingPort;
    private final ObjectMapper objectMapper;
    private final String queue;

    public OrderAdapter(MessagingPort messagingPort,
                        ObjectMapper objectMapper,
                        @Value("${aws.sqs.order-cancel.queue}") String queue) {
        this.messagingPort = messagingPort;
        this.objectMapper = objectMapper;
        this.queue = queue;
    }

    public Mono<SendMessageResponse> cancelOrder(Order order) {
        return messagingPort.send(queue, order, serializePayload());
    }

    private <T> CheckedFunction1<T, String> serializePayload() {
        return objectMapper::writeValueAsString;
    }
}
