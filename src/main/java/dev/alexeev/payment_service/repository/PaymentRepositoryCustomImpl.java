package dev.alexeev.payment_service.repository;

import dev.alexeev.payment_service.entity.Payment;
import dev.alexeev.payment_service.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PaymentRepositoryCustomImpl implements PaymentRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Payment> search(String userId, String orderId, PaymentStatus status, Pageable pageable) {
        List<Criteria> filters = new ArrayList<>();
        if (userId != null) filters.add(Criteria.where("userId").is(userId));
        if (orderId != null) filters.add(Criteria.where("orderId").is(orderId));
        if (status != null) filters.add(Criteria.where("status").is(status));

        Query query = new Query();
        if (!filters.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(filters));
        }

        long total = mongoTemplate.count(query, Payment.class);
        query.with(pageable);
        List<Payment> content = mongoTemplate.find(query, Payment.class);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public BigDecimal sumAmount(String userId, PaymentStatus status, Instant fromInclusive, Instant toExclusive) {
        Criteria criteria = Criteria.where("status").is(status)
                .and("timestamp").gte(fromInclusive).lt(toExclusive);
        if (userId != null) {
            criteria = criteria.and("userId").is(userId);
        }

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
}