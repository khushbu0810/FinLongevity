package com.example.payment_service.kafka;

import com.example.core.events.PaymentStatusEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentStatusEventPublisher {

    public static final String PAYMENT_STATUS_TOPIC = "payment-status-events";

    private final KafkaTemplate<String, PaymentStatusEvent> kafkaTemplate;

    public PaymentStatusEventPublisher(KafkaTemplate<String, PaymentStatusEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes a payment status event.
     * Use the payment ID as the Kafka key to keep events for a payment ordered
     * within the same partition.
     */
    public void publish(PaymentStatusEvent event) {
        String key = event.getPaymentId() == null
                ? event.getEventId().toString()
                : event.getPaymentId().toString();

        kafkaTemplate.send(PAYMENT_STATUS_TOPIC, key, event);
    }
}