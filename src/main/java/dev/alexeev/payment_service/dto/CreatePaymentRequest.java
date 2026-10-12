package dev.alexeev.payment_service.dto;

import dev.alexeev.payment_service.entity.PaymentStatus;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotBlank String orderId,
        @NotBlank String userId,
        @NotNull @Positive @Digits(integer = 15, fraction = 2) BigDecimal paymentAmount,
        PaymentStatus status   // optional, defaults to PENDING
) {
}