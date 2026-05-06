package com.vietnl.paymentservice.infrastructure.persistence.converters;

import com.vietnl.paymentservice.domain.models.enums.PaymentStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PaymentStatusConverter extends IConverter<PaymentStatus> {
}
