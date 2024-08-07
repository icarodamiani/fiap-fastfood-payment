package io.fiap.fastfood.driven.core.domain.order.port.outbound;

import io.fiap.fastfood.driven.core.domain.model.Order;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

public interface OrderPort {

    Mono<SendMessageResponse> cancelOrder(Order order);

}
