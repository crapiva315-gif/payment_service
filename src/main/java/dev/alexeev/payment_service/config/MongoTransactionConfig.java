package dev.alexeev.payment_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

/**
 * Multi-document transactions (payment + outbox row in one commit). They need MongoDB to run as a
 * replica set, even a single-node one - see compose.yaml.
 */
@Configuration
public class MongoTransactionConfig {

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory databaseFactory) {
        return new MongoTransactionManager(databaseFactory);
    }
}