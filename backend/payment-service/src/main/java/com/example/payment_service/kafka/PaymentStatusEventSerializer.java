
package com.example.payment_service.kafka;

import com.example.core.events.PaymentStatusEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class PaymentStatusEventSerializer implements Serializer<PaymentStatusEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public byte[] serialize(String topic, PaymentStatusEvent event) {
        if (event == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsBytes(event);
        } catch (Exception e) {
            throw new SerializationException(
                    "Failed to serialize PaymentStatusEvent", e
            );
        }
    }
}
