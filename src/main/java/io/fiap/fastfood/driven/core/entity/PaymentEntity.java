package io.fiap.fastfood.driven.core.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("payment")
public record PaymentEntity(
    @Id
    String id,
    @Field
    String method,
    @Field
    BigDecimal total,
    @Field
    LocalDateTime dateTime,
    @Field
    String orderId,
    @Field
    Boolean paid) {

    public static final class PaymentEntityBuilder {
        private String id;
        private String method;
        private BigDecimal total;
        private LocalDateTime dateTime;
        private String orderId;
        private Boolean paid;

        private PaymentEntityBuilder() {
        }

        public static PaymentEntityBuilder builder() {
            return new PaymentEntityBuilder();
        }

        public PaymentEntityBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public PaymentEntityBuilder withMethod(String method) {
            this.method = method;
            return this;
        }

        public PaymentEntityBuilder withTotal(BigDecimal amount) {
            this.total = amount;
            return this;
        }

        public PaymentEntityBuilder withDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public PaymentEntityBuilder withOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public PaymentEntityBuilder withPaid(Boolean paid) {
            this.paid = paid;
            return this;
        }

        public PaymentEntity build() {
            return new PaymentEntity(id, method, total, dateTime, orderId, paid);
        }
    }

}
