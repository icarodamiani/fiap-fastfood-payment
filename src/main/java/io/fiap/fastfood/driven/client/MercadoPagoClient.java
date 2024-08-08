package io.fiap.fastfood.driven.client;

import io.fiap.fastfood.driven.client.dto.CreatePaymentRequest;
import io.fiap.fastfood.driven.client.dto.PaymentResponse;
import io.fiap.fastfood.driven.core.domain.model.Payment;
import io.fiap.fastfood.driven.core.exception.BusinessException;
import io.fiap.fastfood.driven.core.exception.NotFoundException;
import io.fiap.fastfood.driven.core.exception.TechnicalException;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MercadoPagoClient {

    private final WebClient client;
    @Value("${payment.webhook}")
    public String webhook;

    public MercadoPagoClient(@Qualifier("MercadoPagoWebClient") WebClient client) {
        this.client = client;
    }

    public Mono<PaymentResponse> createPayment(Payment payment) {
        return Mono.just(
            CreatePaymentRequest.CreatePaymentRequestBuilder.builder()
                .withId(payment.id())
                .withAmount(payment.total())
                .withMethod("MercadoPago")
                .withOrderNumber(payment.orderNumber())
                .withStatus("WAITING")
                .withDateTime(LocalDateTime.now())
                .withWebhook(webhook)
                .build()
        ).flatMap(request ->
            client
                .post()
                .uri("/v1/payment/create")
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .onStatus(status -> HttpStatus.NOT_FOUND == status,
                    clientResponse ->
                        clientResponse.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new NotFoundException(body)))
                )
                .onStatus(HttpStatusCode::is4xxClientError,
                    clientResponse ->
                        clientResponse.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new BusinessException(body)))
                )
                .onStatus(HttpStatusCode::is5xxServerError,
                    clientResponse ->
                        clientResponse.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new TechnicalException(body)))
                )
                .bodyToMono(PaymentResponse.class)
        );
    }
}
