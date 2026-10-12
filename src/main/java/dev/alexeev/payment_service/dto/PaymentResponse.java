package dev.alexeev.payment_service.dto;

import dev.alexeev.payment_service.entity.Payment;
import dev.alexeev.payment_service.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String id,
        String orderId,
        String userId,
        PaymentStatus status,
        Instant timestamp,
        BigDecimal paymentAmount
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getId(), p.getOrderId(), p.getUserId(),
                p.getStatus(), p.getTimestamp(), p.getPaymentAmount());
    }
}