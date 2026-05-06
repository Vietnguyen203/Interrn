package com.vietnl.paymentservice.application.validators;

import com.vietnl.paymentservice.application.dto.request.CreatePaymentRequest;
import com.vietnl.paymentservice.domain.models.enums.ExceptionMessage;
import com.vietnl.paymentservice.infrastructure.persistence.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentValidator {

    private final PaymentRepository paymentRepository;

    public void validateCreate(CreatePaymentRequest request) {
        // Kiểm tra đơn hàng đã tồn tại bản ghi thanh toán chưa
        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new RuntimeException(ExceptionMessage.DUPLICATE_PAYMENT.getMessage());
        }

        // Kiểm tra số tiền
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(ExceptionMessage.INVALID_AMOUNT.getMessage());
        }

        // Kiểm tra phương thức thanh toán
        if (request.getMethod() == null) {
            throw new RuntimeException(String.format(ExceptionMessage.MISSING_REQUIRED_FIELD.getMessage(), "method"));
        }
    }
}
