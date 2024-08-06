package io.fiap.fastfood.driven.core.domain.tracking.port.outbound;

import io.fiap.fastfood.driven.core.domain.model.OrderTracking;
import io.fiap.fastfood.driven.core.domain.model.Payment;
import reactor.core.publisher.Mono;

public interface OrderTrackingPort {

    Mono<Void> create(OrderTracking tracking);

}
