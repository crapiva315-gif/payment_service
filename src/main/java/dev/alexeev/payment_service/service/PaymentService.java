package dev.alexeev.payment_service.service;

import dev.alexeev.payment_service.dto.CreatePaymentRequest;
import dev.alexeev.payment_service.dto.PageResponse;
import dev.alexeev.payment_service.dto.PaymentResponse;
import dev.alexeev.payment_service.dto.TotalAmountResponse;
import dev.alexeev.payment_service.entity.Payment;
import dev.alexeev.payment_service.entity.PaymentStatus;
import dev.alexeev.payment_service.exception.BadRequestException;
import dev.alexeev.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final int MAX_PAGE_SIZE = 100;

    private final PaymentRepository paymentRepository;
    private final MongoTemplate mongoTemplate;

    public PaymentResponse create(CreatePaymentRequest request) {
        Payment payment = Payment.builder()
                .id(UUID.randomUUID().toString())
                .orderId(request.orderId())
                .userId(request.userId())
                .status(request.status() != null ? request.status() : PaymentStatus.PENDING)
                .paymentAmount(request.paymentAmount())
                .timestamp(Instant.now().truncatedTo(ChronoUnit.MILLIS))
                .build();

        Payment saved = paymentRepository.insert(payment);
        log.info("Payment created: id={}, orderId={}, userId={}, status={}",
                saved.getId(), saved.getOrderId(), saved.getUserId(), saved.getStatus());
        return PaymentResponse.from(saved);
    }

    public PageResponse<PaymentResponse> search(String userId, String orderId, PaymentStatus status,
                                                int page, int size) {
        if (userId == null && orderId == null && status == null) {
            throw new BadRequestException("Provide at least one of: userId, orderId, status");
        }
        if (page < 0 || size < 1) {
            throw new BadRequestException("page must be >= 0 and size must be >= 1");
        }
        int pageSize = Math.min(size, MAX_PAGE_SIZE);

        List<Criteria> filters = new ArrayList<>();
        if (userId != null) filters.add(Criteria.where("userId").is(userId));
        if (orderId != null) filters.add(Criteria.where("orderId").is(orderId));
        if (status != null) filters.add(Criteria.where("status").is(status));

        Query query = new Query(new Criteria().andOperator(filters));
        long total = mongoTemplate.count(query, Payment.class);

        query.with(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "timestamp")));
        List<PaymentResponse> content = mongoTemplate.find(query, Payment.class).stream()
                .map(PaymentResponse::from)
                .toList();

        int totalPages = (int) Math.ceil((double) total / pageSize);
        return new PageResponse<>(content, page, pageSize, total, totalPages);
    }

    public TotalAmountResponse totalForUser(String userId, LocalDate from, LocalDate to) {
        validateRange(from, to);
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("status").is(PaymentStatus.COMPLETED)
                .and("timestamp").gte(startOf(from)).lt(endExclusiveOf(to));
        return new TotalAmountResponse(userId, from, to, sum(criteria));
    }

    public TotalAmountResponse totalForAllUsers(LocalDate from, LocalDate to) {
        validateRange(from, to);
        Criteria criteria = Criteria.where("status").is(PaymentStatus.COMPLETED)
                .and("timestamp").gte(startOf(from)).lt(endExclusiveOf(to));
        return new TotalAmountResponse(null, from, to, sum(criteria));
    }

    private BigDecimal sum(Criteria criteria) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group().sum("paymentAmount").as("total"));

        Document result = mongoTemplate
                .aggregate(aggregation, Payment.class, Document.class)
                .getUniqueMappedResult();

        if (result == null) {
            return BigDecimal.ZERO;   // no payments in the range
        }
        Object total = result.get("total");
        if (total instanceof Decimal128 d) {
            return d.bigDecimalValue();
        }
        if (total instanceof Number n) {
            return new BigDecimal(n.toString());
        }
        return BigDecimal.ZERO;
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BadRequestException("'from' must not be after 'to'");
        }
    }

    private Instant startOf(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private Instant endExclusiveOf(LocalDate date) {
        return date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}