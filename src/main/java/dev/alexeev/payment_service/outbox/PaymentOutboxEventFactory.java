package dev.alexeev.payment_service.outbox;

import dev.alexeev.payment_service.entity.OutboxEvent;
import dev.alexeev.payment_service.entity.Payment;
import dev.alexeev.payment_service.event.PaymentEvent;
import dev.alexeev.payment_service.event.PaymentEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Builds the outbox row for a payment. Debezium turns the row into a Kafka message. */
@Component
@RequiredArgsConstructor
public class PaymentOutboxEventFactory {

    public static final String AGGREGATE_TYPE = "payment";

    private final ObjectMapper objectMapper;

    public OutboxEvent paymentCreated(Payment payment) {
        String eventId = UUID.randomUUID().toString();
        PaymentEvent event = new PaymentEvent(
                eventId,
                PaymentEventType.CREATE_PAYMENT,
                payment.getTimestamp(),
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getStatus(),
                payment.getPaymentAmount());

        return OutboxEvent.builder()
                .id(eventId)
                .aggregateType(AGGREGATE_TYPE)
                .aggregateId(payment.getOrderId())
                .type(PaymentEventType.CREATE_PAYMENT.name())
                .payload(objectMapper.writeValueAsString(event))
                .createdAt(Instant.now().truncatedTo(ChronoUnit.MILLIS))
                .build();
    }
}