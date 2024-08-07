package io.fiap.fastfood.driven.core.domain.tracking.port.outbound;

import io.fiap.fastfood.driven.core.domain.model.OrderTracking;
import reactor.core.publisher.Mono;

public interface TrackingPort {

    Mono<Void> create(OrderTracking tracking);

}
