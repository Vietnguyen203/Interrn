package com.vietnl.paymentservice.infrastructure.persistence.converters;

import com.vietnl.paymentservice.domain.models.enums.PaymentMethod;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PaymentMethodConverter extends IConverter<PaymentMethod> {
}
