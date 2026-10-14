package dev.alexeev.payment_service.service;

import dev.alexeev.payment_service.client.RandomNumberClient;
import dev.alexeev.payment_service.dto.CreatePaymentRequest;
import dev.alexeev.payment_service.dto.DateRangeRequest;
import dev.alexeev.payment_service.dto.PageResponse;
import dev.alexeev.payment_service.dto.PaymentResponse;
import dev.alexeev.payment_service.dto.PaymentSearchRequest;
import dev.alexeev.payment_service.dto.TotalAmountResponse;
import dev.alexeev.payment_service.entity.Payment;
import dev.alexeev.payment_service.entity.PaymentStatus;
import dev.alexeev.payment_service.mapper.PaymentMapper;
import dev.alexeev.payment_service.outbox.PaymentOutboxEventFactory;
import dev.alexeev.payment_service.repository.OutboxEventRepository;
import dev.alexeev.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/** Takes and returns DTOs; talks to the DAO layer with entities (conversion via MapStruct). */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RandomNumberClient randomNumberClient;
    private final OutboxEventRepository outboxEventRepository;
    private final PaymentOutboxEventFactory outboxEventFactory;
    private final TransactionTemplate transactionTemplate;

    // 1. Create: even random number -> SUCCESS, odd -> FAILED
    public PaymentResponse create(CreatePaymentRequest request) {
        // call the external API first, outside of the transaction: if it fails, nothing is saved
        int number = randomNumberClient.nextNumber();

        Payment payment = paymentMapper.toEntity(request);
        payment.setId(UUID.randomUUID().toString());
        // MongoDB stores dates with millisecond precision
        payment.setTimestamp(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        payment.setStatus(number % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);

        // transactional outbox: the payment and its CREATE_PAYMENT event are committed together or not at all;
        // Debezium reads the outbox collection and publishes the event to Kafka
        Payment saved = Objects.requireNonNull(transactionTemplate.execute(status -> {
            Payment persisted = paymentRepository.insert(payment);
            outboxEventRepository.insert(outboxEventFactory.paymentCreated(persisted));
            return persisted;
        }));

        log.info("Payment created: id={}, orderId={}, userId={}, status={} (random number {})",
                saved.getId(), saved.getOrderId(), saved.getUserId(), saved.getStatus(), number);
        return paymentMapper.toResponse(saved);
    }

    // 2. Search by userId and/or orderId and/or status
    public PageResponse<PaymentResponse> search(PaymentSearchRequest request) {
        Pageable pageable = PageRequest.of(request.page(), request.size(),
                Sort.by(Sort.Direction.DESC, "timestamp"));

        Page<Payment> page = paymentRepository.search(
                request.userId(),
                request.orderId(),
                paymentMapper.toEntityStatus(request.status()),
                pageable);

        return new PageResponse<>(
                paymentMapper.toResponses(page.getContent()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    // 3. Total for one user (SUCCESS payments only)
    public TotalAmountResponse totalForUser(String userId, DateRangeRequest range) {
        BigDecimal total = paymentRepository.sumAmount(
                userId, PaymentStatus.SUCCESS, startOf(range.from()), endExclusiveOf(range.to()));
        return new TotalAmountResponse(userId, range.from(), range.to(), total);
    }

    // 4. Total for all users (admin, SUCCESS payments only)
    public TotalAmountResponse totalForAllUsers(DateRangeRequest range) {
        BigDecimal total = paymentRepository.sumAmount(
                null, PaymentStatus.SUCCESS, startOf(range.from()), endExclusiveOf(range.to()));
        return new TotalAmountResponse(null, range.from(), range.to(), total);
    }

    private Instant startOf(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private Instant endExclusiveOf(LocalDate date) {
        return date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}