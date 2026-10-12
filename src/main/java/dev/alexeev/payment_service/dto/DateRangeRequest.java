package dev.alexeev.payment_service.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record DateRangeRequest(

        @NotNull(message = "from is required (yyyy-MM-dd)")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @NotNull(message = "to is required (yyyy-MM-dd)")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to
) {
    @AssertTrue(message = "'from' must not be after 'to'")
    public boolean isRangeValid() {
        return from == null || to == null || !from.isAfter(to);
    }
}