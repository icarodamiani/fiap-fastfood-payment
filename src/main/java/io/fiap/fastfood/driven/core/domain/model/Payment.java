package io.fiap.fastfood.driven.core.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record Payment(
    String id,
    String method,
    BigDecimal total,
    LocalDateTime dateTime,
    String orderId,
    PaymentStatus status) {


    public static final class PaymentBuilder {
        private String id;
        private String method;
        private BigDecimal total;
        private LocalDateTime dateTime;
        private String orderId;
        private PaymentStatus status;

        private PaymentBuilder() {
        }

        public static PaymentBuilder builder() {
            return new PaymentBuilder();
        }

        public static PaymentBuilder from(Payment payment) {
            return PaymentBuilder.builder()
                .withId(payment.id)
                .withOrderId(payment.orderId)
                .withDateTime(payment.dateTime)
                .withMethod(payment.method)
                .withTotal(payment.total)
                .withStatus(payment.status);
        }

        public PaymentBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public PaymentBuilder withMethod(String method) {
            this.method = method;
            return this;
        }

        public PaymentBuilder withTotal(BigDecimal total) {
            this.total = total;
            return this;
        }

        public PaymentBuilder withDateTime(LocalDateTime date) {
            this.dateTime = date;
            return this;
        }

        public PaymentBuilder withOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public PaymentBuilder withStatus(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public Payment build() {
            return new Payment(id, method, total, dateTime, orderId, status);
        }
    }
}
