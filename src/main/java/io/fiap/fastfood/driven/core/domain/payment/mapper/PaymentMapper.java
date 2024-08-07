package io.fiap.fastfood.driven.core.domain.payment.mapper;

import io.fiap.fastfood.driven.core.domain.model.Payment;
import io.fiap.fastfood.driven.core.entity.PaymentEntity;
import io.fiap.fastfood.driver.controller.payment.dto.PaymentDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentEntity entityFromDomain(Payment payment);

    Payment domainFromEntity(PaymentEntity paymentEntity);

    Payment domainFromDto(PaymentDTO paymentDTO);
}