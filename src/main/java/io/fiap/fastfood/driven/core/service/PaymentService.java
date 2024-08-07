package io.fiap.fastfood.driven.core.service;

import io.fiap.fastfood.driven.core.domain.model.Notification;
import io.fiap.fastfood.driven.core.domain.model.Order;
import io.fiap.fastfood.driven.core.domain.model.Payment;
import io.fiap.fastfood.driven.core.domain.model.PaymentStatus;
import io.fiap.fastfood.driven.core.domain.notification.port.outbound.NotificationPort;
import io.fiap.fastfood.driven.core.domain.order.port.outbound.OrderPort;
import io.fiap.fastfood.driven.core.domain.payment.port.inbound.PaymentUseCase;
import io.fiap.fastfood.driven.core.domain.payment.port.outbound.PaymentPort;
import io.vavr.Function1;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

@Service
public class PaymentService implements PaymentUseCase {

    private static final String UPDATE_STATUS_OPERATION =
        "[ { \"op\": \"replace\", \"path\": \"/paid\", \"value\": \"{paid.value}\" } ]";

    private final PaymentPort paymentPort;
    private final NotificationPort notificationPort;
    private final OrderPort orderPort;

    public PaymentService(PaymentPort paymentPort, NotificationPort notificationPort, OrderPort orderPort) {
        this.paymentPort = paymentPort;
        this.notificationPort = notificationPort;
        this.orderPort = orderPort;
    }


    @Override
    public Mono<Void> update(Payment payment) {
        return paymentPort.updatePayment(
                payment.id(),
                UPDATE_STATUS_OPERATION.replace("{paid.value}", "true")
            )
            .filter(p -> p.status() == PaymentStatus.FAILED)
            .flatMap(p ->
                orderPort.cancelOrder(new Order(p.orderId()))
                    .doOnSuccess(unused -> notificationPort.sendNotification(new Notification()))
                    .map(__ -> p)
            )
            .switchIfEmpty(Mono.defer(() -> Mono.just(payment)))
            .then();
    }

    @Override
    public Flux<Message> handleEvent() {
        return paymentPort.readPayment(handle());
    }

    private Function1<Payment, Mono<Payment>> handle() {
        return customer -> Mono.just(customer)
            .flatMap(paymentPort::createPayment);
    }

}
