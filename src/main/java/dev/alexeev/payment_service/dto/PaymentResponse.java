package dev.alexeev.payment_service.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String id,
        String orderId,
        String userId,
        PaymentStatusDto status,
        Instant timestamp,
        BigDecimal paymentAmount
) {
}