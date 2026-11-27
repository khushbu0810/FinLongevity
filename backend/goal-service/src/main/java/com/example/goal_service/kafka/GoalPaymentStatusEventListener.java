package com.example.goal_service.kafka;

import com.example.core.events.PaymentStatusEvent;
import com.example.goal_service.service.GoalService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class GoalPaymentStatusEventListener {

    private static final Logger log = LoggerFactory.getLogger(GoalPaymentStatusEventListener.class);


    private final GoalService goalService;

    public GoalPaymentStatusEventListener(GoalService goalService) {
        this.goalService = goalService;
    }

    @KafkaListener(
            topics = "payment-status-events",
            groupId = "${spring.kafka.consumer.group-id:goal-service-group}"
    )
    public void consume(PaymentStatusEvent event) {

        log.info(
                "Received payment event: paymentId={}, businessReference={}, status={}",
                event.getPaymentId(),
                event.getBusinessReference(),
                event.getStatus()
        );

        goalService.handlePaymentStatusEvent(event);
    }


    @PostConstruct
    public void verifyListenerBean() {
        log.info("GOAL PAYMENT KAFKA LISTENER BEAN CREATED");
    }
}
