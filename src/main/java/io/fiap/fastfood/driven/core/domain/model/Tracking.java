package io.fiap.fastfood.driven.core.domain.model;

public record Tracking(
        String orderId,
        String orderStatus,
        String orderStatusValue
) {

    public static final class OrderTrackingBuilder {
        private String orderId;
        private String orderStatus;
        private String orderStatusValue;

        private OrderTrackingBuilder() {
        }

        public static OrderTrackingBuilder builder() {
            return new OrderTrackingBuilder();
        }

        public static OrderTrackingBuilder from(Tracking tracking) {
            return OrderTrackingBuilder.builder()
                .withOrderId(tracking.orderId)
                .withOrderStatus(tracking.orderStatus)
                .withOrderStatusValue(tracking.orderStatusValue);
        }


        public OrderTrackingBuilder withOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }


        public OrderTrackingBuilder withOrderStatus(String orderStatus) {
            this.orderStatus = orderStatus;
            return this;
        }

        public OrderTrackingBuilder withOrderStatusValue(String orderStatusValue) {
            this.orderStatusValue = orderStatusValue;
            return this;
        }


        public Tracking build() {
            return new Tracking(orderId, orderStatus, orderStatusValue);
        }
    }
}
