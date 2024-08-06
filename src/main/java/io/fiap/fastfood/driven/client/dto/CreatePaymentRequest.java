package io.fiap.fastfood.driven.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreatePaymentRequest(
    String id,
    String method,
    BigDecimal amount,
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.yyyy'Z'")
    LocalDateTime dateTime,
    String orderId,
    String webhook,
    String status) {


    public static final class CreatePaymentRequestBuilder {
        private String id;
        private String method;
        private BigDecimal amount;
        private LocalDateTime dateTime;
        private String orderId;
        private String webhook;
        private String status;

        private CreatePaymentRequestBuilder() {
        }

        public static CreatePaymentRequestBuilder builder() {
            return new CreatePaymentRequestBuilder();
        }

        public CreatePaymentRequestBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public CreatePaymentRequestBuilder withMethod(String method) {
            this.method = method;
            return this;
        }

        public CreatePaymentRequestBuilder withAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public CreatePaymentRequestBuilder withDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public CreatePaymentRequestBuilder withOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public CreatePaymentRequestBuilder withWebhook(String webhook) {
            this.webhook = webhook;
            return this;
        }

        public CreatePaymentRequestBuilder withStatus(String status) {
            this.status = status;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(id, method, amount, dateTime, orderId, webhook, status);
        }
    }
}
