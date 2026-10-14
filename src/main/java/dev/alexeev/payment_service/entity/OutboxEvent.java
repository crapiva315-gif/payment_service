package dev.alexeev.payment_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

/**
 * A row of the outbox collection. The field names (aggregatetype, aggregateid, type, payload)
 * are the ones the Debezium MongoDB outbox event router expects by default.
 * The application only INSERTS here; Debezium reads the change stream and publishes to Kafka.
 */
@Document(collection = "outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    /** Unique event id; Debezium puts it into the "id" header of the Kafka message. */
    @Id
    private String id;

    /** Used for topic routing in the connector config. */
    @Field("aggregatetype")
    private String aggregateType;

    /** Becomes the Kafka message key (we use orderId, so events of one order stay ordered). */
    @Field("aggregateid")
    private String aggregateId;

    /** Event type, e.g. CREATE_PAYMENT (added as the "eventType" header). */
    private String type;

    /** The event as a JSON string: it becomes the Kafka message value as is. */
    private String payload;

    /** Used by the TTL index that removes old rows. */
    @Field("created_at")
    private Instant createdAt;
}