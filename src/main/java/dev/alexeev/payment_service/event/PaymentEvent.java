package dev.alexeev.payment_service.event;

import dev.alexeev.payment_service.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Message published to Kafka when a payment is created. Serialized to JSON, key = orderId.
 * The consumer (order_service) keeps its own copy of this contract.
 */
public record PaymentEvent(
        String eventId,
        PaymentEventType eventType,
        Instant occurredAt,
        String paymentId,
        String orderId,
        String userId,
        PaymentStatus status,
        BigDecimal paymentAmount
) {
}