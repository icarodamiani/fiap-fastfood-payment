package io.fiap.fastfood.driven.core.domain.model;

public record PaymentStatus(
    Long id,
    Long paymentId,
    String description) {
}
