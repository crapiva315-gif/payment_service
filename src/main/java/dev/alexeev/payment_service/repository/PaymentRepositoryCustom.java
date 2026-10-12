package dev.alexeev.payment_service.repository;

import dev.alexeev.payment_service.entity.Payment;
import dev.alexeev.payment_service.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;

public interface PaymentRepositoryCustom {

  Page<Payment> search(String userId, String orderId, PaymentStatus status, Pageable pageable);

  BigDecimal sumAmount(String userId, PaymentStatus status, Instant fromInclusive, Instant toExclusive);
}