package dev.alexeev.payment_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatePaymentRequest(

        @NotBlank(message = "orderId must not be blank")
        @Size(max = 64, message = "orderId must be at most 64 characters")
        String orderId,

        @NotBlank(message = "userId must not be blank")
        @Size(max = 64, message = "userId must be at most 64 characters")
        String userId,

        @NotNull(message = "paymentAmount is required")
        @DecimalMin(value = "0.01", message = "paymentAmount must be at least 0.01")
        @Digits(integer = 15, fraction = 2, message = "paymentAmount: up to 15 integer and 2 fraction digits")
        BigDecimal paymentAmount
) {
}