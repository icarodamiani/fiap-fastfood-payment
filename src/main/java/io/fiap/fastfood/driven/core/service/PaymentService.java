package io.fiap.fastfood.driven.core.service;

import io.fiap.fastfood.driven.core.domain.model.Payment;
import io.fiap.fastfood.driven.core.domain.payment.port.inbound.PaymentUseCase;
import io.fiap.fastfood.driven.core.domain.payment.port.outbound.PaymentPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PaymentService implements PaymentUseCase {

    private static final String UPDATE_STATUS_OPERATION =
        "[ { \"op\": \"replace\", \"path\": \"/paid\", \"value\": \"{paid.value}\" } ]";

    private final PaymentPort paymentPort;

    public PaymentService(PaymentPort paymentPort) {this.paymentPort = paymentPort;}


    @Override
    public Mono<Void> update(Payment payment) {
        return paymentPort.updatePayment(
                payment.id(),
                UPDATE_STATUS_OPERATION.replace("{paid.value}", "true")
            )
            .then();
    }
}
