package dev.alexeev.payment_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TotalAmountResponse(
        String userId,
        LocalDate from,
        LocalDate to,
        BigDecimal totalAmount
) {
}