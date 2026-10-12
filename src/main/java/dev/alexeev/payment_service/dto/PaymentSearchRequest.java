package dev.alexeev.payment_service.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record PaymentSearchRequest(

        @Size(min = 1, max = 64, message = "userId must be 1-64 characters")
        String userId,

        @Size(min = 1, max = 64, message = "orderId must be 1-64 characters")
        String orderId,

        PaymentStatusDto status,

        @Min(value = 0, message = "page must be >= 0")
        Integer page,

        @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100")
        Integer size
) {
    public PaymentSearchRequest {
        if (page == null) page = 0;
        if (size == null) size = 20;
    }

    @AssertTrue(message = "At least one of userId, orderId, status must be provided")
    public boolean isAnyFilterPresent() {
        return userId != null || orderId != null || status != null;
    }
}