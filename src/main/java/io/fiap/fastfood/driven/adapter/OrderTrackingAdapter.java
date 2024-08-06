package io.fiap.fastfood.driven.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fiap.fastfood.driven.core.domain.model.OrderTracking;
import io.fiap.fastfood.driven.core.domain.tracking.port.outbound.OrderTrackingPort;
import io.vavr.CheckedFunction1;
import io.vavr.Function1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
public class OrderTrackingAdapter implements OrderTrackingPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderTrackingAdapter.class);

    private final SqsAsyncClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String queue;

    public OrderTrackingAdapter(SqsAsyncClient sqsClient,
                                ObjectMapper objectMapper,
                                @Value("${aws.sqs.tracking.queue}") String queue) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.queue = queue;
    }

    public Mono<Void> create(OrderTracking tracking) {
        return Mono.just(serializePayload().unchecked().apply(tracking))
            .zipWith(getQueueUrl().apply(queue))
            .map(t -> buildMessageRequest().unchecked().apply(t))
            .doOnError(throwable -> LOGGER.error("Failed to prepare message due to error.", throwable))
            .flatMap(message -> Mono.fromFuture(sqsClient.sendMessage(message)))
            .doOnError(throwable -> LOGGER.error("Failed to send message due to error.", throwable))
            .doOnSuccess(response ->
                LOGGER.debug("Message published to queue. Message ID: {} Body: {}", response.messageId(),
                    response.md5OfMessageBody()))
            .then();
    }

    private <T> CheckedFunction1<T, String> serializePayload() {
        return objectMapper::writeValueAsString;
    }

    private Function1<String, Mono<GetQueueUrlResponse>> getQueueUrl() {
        return queueName -> Mono.fromFuture(sqsClient.getQueueUrl(GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build()))
            .doOnError(throwable -> LOGGER.error("Failed to get queueUrl", throwable));
    }

    private CheckedFunction1<Tuple2<String, GetQueueUrlResponse>, SendMessageRequest> buildMessageRequest() {
        return t -> SendMessageRequest.builder()
            .messageBody(t.getT1())
            .queueUrl(t.getT2().queueUrl())
            .build();
    }
}
