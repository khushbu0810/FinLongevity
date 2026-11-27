
package com.example.goal_service.config;

import com.example.core.events.PaymentStatusEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, PaymentStatusEvent> paymentStatusConsumerFactory(
            ObjectMapper objectMapper) {

        Deserializer<PaymentStatusEvent> eventDeserializer =
                new Deserializer<>() {

                    @Override
                    public PaymentStatusEvent deserialize(
                            String topic, byte[] data) {

                        if (data == null) {
                            return null;
                        }

                        try {
                            return objectMapper.readValue(
                                    data, PaymentStatusEvent.class);
                        } catch (Exception exception) {
                            throw new IllegalArgumentException(
                                    "Failed to deserialize PaymentStatusEvent",
                                    exception);
                        }
                    }
                };

        Map<String, Object> config = new HashMap<>();

        config.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092");

        config.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "goal-service-group");

        config.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest");

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                eventDeserializer);
    }

    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, PaymentStatusEvent>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentStatusEvent> paymentStatusConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, PaymentStatusEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(paymentStatusConsumerFactory);

        return factory;
    }
}
