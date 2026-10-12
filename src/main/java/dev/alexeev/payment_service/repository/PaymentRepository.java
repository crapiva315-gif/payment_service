package dev.alexeev.payment_service.repository;

import dev.alexeev.payment_service.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<Payment, String>, PaymentRepositoryCustom {
}