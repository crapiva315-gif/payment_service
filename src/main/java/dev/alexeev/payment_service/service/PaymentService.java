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
import dev.alexeev.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RandomNumberClient randomNumberClient;

    public PaymentResponse create(CreatePaymentRequest request) {
        int number = randomNumberClient.nextNumber();

        Payment payment = paymentMapper.toEntity(request);
        payment.setId(UUID.randomUUID().toString());
        payment.setTimestamp(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        payment.setStatus(number % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);

        Payment saved = paymentRepository.insert(payment);
        log.info("Payment created: id={}, orderId={}, userId={}, status={} (random number {})",
                saved.getId(), saved.getOrderId(), saved.getUserId(), saved.getStatus(), number);
        return paymentMapper.toResponse(saved);
    }

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

    public TotalAmountResponse totalForUser(String userId, DateRangeRequest range) {
        BigDecimal total = paymentRepository.sumAmount(
                userId, PaymentStatus.SUCCESS, startOf(range.from()), endExclusiveOf(range.to()));
        return new TotalAmountResponse(userId, range.from(), range.to(), total);
    }

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