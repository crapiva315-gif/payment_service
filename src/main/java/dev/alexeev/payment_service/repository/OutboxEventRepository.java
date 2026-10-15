package dev.alexeev.payment_service.repository;

import dev.alexeev.payment_service.entity.OutboxEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OutboxEventRepository extends MongoRepository<OutboxEvent, String> {
}