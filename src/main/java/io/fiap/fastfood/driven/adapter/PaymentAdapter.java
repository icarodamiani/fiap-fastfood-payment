package io.fiap.fastfood.driven.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.fiap.fastfood.driven.client.MercadoPagoClient;
import io.fiap.fastfood.driven.core.domain.model.OrderTracking;
import io.fiap.fastfood.driven.core.domain.model.Payment;
import io.fiap.fastfood.driven.core.domain.payment.mapper.PaymentMapper;
import io.fiap.fastfood.driven.core.domain.payment.port.outbound.PaymentPort;
import io.fiap.fastfood.driven.core.domain.tracking.port.outbound.TrackingPort;
import io.fiap.fastfood.driven.core.entity.PaymentEntity;
import io.fiap.fastfood.driven.core.exception.BadRequestException;
import io.fiap.fastfood.driven.core.exception.DuplicatedKeyException;
import io.fiap.fastfood.driven.core.messaging.MessagingPort;
import io.fiap.fastfood.driven.repository.PaymentRepository;
import io.vavr.CheckedFunction1;
import io.vavr.CheckedFunction2;
import io.vavr.Function1;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

@Service
public class PaymentAdapter implements PaymentPort {

    private final String queue;
    private final MessagingPort messagingPort;

    private final MercadoPagoClient paymentClient;
    private final TrackingPort orderTrackingPort;

    private final PaymentRepository repository;
    private final PaymentMapper mapper;
    private final ObjectMapper objectMapper;

    public PaymentAdapter(@Value("${aws.sqs.payment.queue}") String queue,
                          MessagingPort messagingPort,
                          PaymentRepository repository,
                          PaymentMapper mapper,
                          ObjectMapper objectMapper,
                          MercadoPagoClient paymentClient,
                          TrackingPort orderTrackingPort) {
        this.queue = queue;
        this.messagingPort = messagingPort;
        this.repository = repository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.paymentClient = paymentClient;
        this.orderTrackingPort = orderTrackingPort;
    }


    @Override
    public Mono<Payment> createPayment(Payment payment) {
        return repository.findByOrderId(payment.orderId())
            .next()
            .flatMap(c -> Mono.defer(() -> Mono.<PaymentEntity>error(DuplicatedKeyException::new)))
            .switchIfEmpty(Mono.defer(() -> repository.save(mapper.entityFromDomain(payment))
                .flatMap(entity -> paymentClient.createPayment(mapper.domainFromEntity(entity))
                    .map(response -> entity))))
            .map(mapper::domainFromEntity);
    }

    @Override
    public Mono<Payment> updatePayment(String id, String operations) {
        return repository.findById(id)
            .map(payment -> applyPatch().unchecked().apply(payment, operations))
            .flatMap(repository::save)
            .map(mapper::domainFromEntity)
            .doOnSuccess(payment ->
                Mono.justOrEmpty(toTracking().apply(payment))
                    .flatMap(orderTrackingPort::create)
            )
            .onErrorMap(JsonPatchException.class::isInstance, BadRequestException::new);
    }

    private CheckedFunction2<PaymentEntity, String, PaymentEntity> applyPatch() {
        return (payment, operations) -> {
            var patch = readOperations()
                .unchecked()
                .apply(operations);

            var patched = patch.apply(objectMapper.convertValue(payment, JsonNode.class));

            return objectMapper.treeToValue(patched, PaymentEntity.class);
        };
    }

    private CheckedFunction1<String, JsonPatch> readOperations() {
        return operations -> {
            final InputStream in = new ByteArrayInputStream(operations.getBytes());
            return objectMapper.readValue(in, JsonPatch.class);
        };
    }

    private Function1<Payment, OrderTracking> toTracking() {
        return payment -> OrderTracking.OrderTrackingBuilder.builder()
            .withOrderId(payment.orderId())
            .withOrderStatus("PAYMENT_CONFIRMED")
            .withOrderStatusValue("2")
            .build();
    }

    @Override
    public Flux<Message> readPayment(Function1<Payment, Mono<Payment>> handle) {
        return messagingPort.read(queue, handle, readEvent());
    }

    private CheckedFunction1<Message, Payment> readEvent() {
        return message -> objectMapper.readValue(message.body(), Payment.class);
    }
}
