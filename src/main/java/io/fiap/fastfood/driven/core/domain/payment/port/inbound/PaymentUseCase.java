package io.fiap.fastfood.driven.core.domain.payment.port.inbound;

import io.fiap.fastfood.driven.core.domain.model.Payment;
import reactor.core.publisher.Mono;

public interface PaymentUseCase {
    Mono<Void> update(Payment payment);
}
