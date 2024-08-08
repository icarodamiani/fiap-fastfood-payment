package io.fiap.fastfood.driven.core.service;

import io.fiap.fastfood.driven.core.domain.model.Order;
import io.fiap.fastfood.driven.core.domain.model.Payment;
import io.fiap.fastfood.driven.core.domain.model.PaymentStatus;
import io.fiap.fastfood.driven.core.domain.model.Tracking;
import io.fiap.fastfood.driven.core.domain.notification.port.outbound.NotificationPort;
import io.fiap.fastfood.driven.core.domain.order.port.outbound.OrderPort;
import io.fiap.fastfood.driven.core.domain.payment.port.inbound.PaymentUseCase;
import io.fiap.fastfood.driven.core.domain.payment.port.outbound.PaymentPort;
import io.fiap.fastfood.driven.core.domain.tracking.port.outbound.TrackingPort;
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
    private final TrackingPort trackingPort;

    public PaymentService(PaymentPort paymentPort,
                          NotificationPort notificationPort,
                          OrderPort orderPort, TrackingPort trackingPort) {
        this.paymentPort = paymentPort;
        this.notificationPort = notificationPort;
        this.orderPort = orderPort;
        this.trackingPort = trackingPort;
    }


    @Override
    public Mono<Payment> update(Payment payment) {
        return paymentPort.updatePayment(
                payment.orderNumber(),
                UPDATE_STATUS_OPERATION.replace("{paid.value}", paymentStatus().apply(payment.status()))
            )
            .flatMap(p ->
                Mono.justOrEmpty(toTracking().apply(payment))
                    .flatMap(trackingPort::create)
                    .map(o -> payment)
            )
            .filter(p -> p.status() == PaymentStatus.FAILED)
            .flatMap(p ->
                orderPort.cancelOrder(new Order(p.orderNumber()))
                    //.doOnSuccess(unused -> notificationPort.sendNotification(new Notification()))
                    .map(__ -> p)
            )
            .switchIfEmpty(Mono.defer(() -> Mono.just(payment)));
    }

    private Function1<Payment, Tracking> toTracking() {
        return payment -> {
            var tracking = Tracking.OrderTrackingBuilder.builder()
                .withOrderNumber(payment.orderNumber())
                .withOrderStatus("PAYMENT_CONFIRMED")
                .withOrderStatusValue("2");
            if (payment.status() == PaymentStatus.FAILED) {
                tracking.withOrderStatus("PAYMENT_FAILED")
                    .withOrderStatusValue("7");
            }
            return tracking.build();
        };
    }

    private Function1<PaymentStatus, String> paymentStatus() {
        return paymentStatus -> paymentStatus == PaymentStatus.FAILED ? "false" : "true";
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
