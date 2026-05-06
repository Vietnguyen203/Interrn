package com.vietnl.paymentservice.application.usecases;

import com.vietnl.paymentservice.application.dto.request.CreatePaymentRequest;
import com.vietnl.paymentservice.application.validators.PaymentValidator;
import com.vietnl.paymentservice.domain.models.entities.Payment;
import com.vietnl.paymentservice.domain.models.enums.PaymentStatus;
import com.vietnl.paymentservice.infrastructure.persistence.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentValidator paymentValidator;

    @Transactional
    public Payment createPayment(CreatePaymentRequest request) {
        paymentValidator.validateCreate(request);
        
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setStatus(PaymentStatus.PENDING);
        return paymentRepository.save(payment);
    }

    public Payment getPaymentByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId).orElse(null);
    }

    @Transactional
    public Payment completePayment(UUID orderId, String transactionCode) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
        
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionCode(transactionCode);
        payment.setPaidAt(LocalDateTime.now());
        
        return paymentRepository.save(payment);
    }
}
