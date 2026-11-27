package com.example.payment_service.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic paymentStatusTopic() {
        return new NewTopic(
                PaymentStatusEventPublisher.PAYMENT_STATUS_TOPIC,
                1,
                (short) 1
        );
    }
}