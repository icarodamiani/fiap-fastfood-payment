package io.fiap.fastfood.driven.core.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import io.fiap.fastfood.driven.client.SqsMessageClient;
import io.fiap.fastfood.driven.core.domain.model.Payment;
import io.fiap.fastfood.driven.core.domain.payment.port.outbound.PaymentPort;
import io.fiap.fastfood.driven.core.exception.BadRequestException;
import io.fiap.fastfood.driven.core.exception.BusinessException;
import io.fiap.fastfood.driven.core.exception.NotFoundException;
import io.vavr.CheckedFunction1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

@Component
public class PaymentMessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentMessageHandler.class);

    private final PaymentPort paymentPort;
    private final SqsMessageClient sqsMessageClient;
    private final ObjectMapper mapper;

    public PaymentMessageHandler(PaymentPort paymentPort, SqsMessageClient sqsMessageClient, ObjectMapper mapper) {
        this.paymentPort = paymentPort;
        this.sqsMessageClient = sqsMessageClient;
        this.mapper = mapper;
    }


    @VisibleForTesting
    public Flux<DeleteMessageResponse> handleEvent() {
        return sqsMessageClient.receive("payment_queue")
            .map(ReceiveMessageResponse::messages)
            .flatMapMany(messages ->
                Flux.fromIterable(messages)
                    .flatMap(message ->
                        getPayment().unchecked().apply(message)
                            .flatMap(paymentPort::createPayment)
                            .map(__ -> message)
                            .onErrorResume(t ->
                                    t instanceof NotFoundException
                                        || t instanceof BusinessException
                                        || t instanceof BadRequestException,
                                throwable -> {
                                    LOGGER.error(throwable.getMessage(), throwable);
                                    return Mono.just(message);
                                }
                            )
                    )
                    .flatMap(message -> sqsMessageClient.delete("payment_queue", message))
            );
    }


    private CheckedFunction1<Message, Mono<Payment>> getPayment() {
        return message -> Mono.justOrEmpty(mapper.readValue(message.body(), Payment.class));
    }
}
