package com.example.investment_service.kafka;

import com.example.core.events.PaymentStatusEvent;
import com.example.investment_service.service.InvestmentPaymentSettlementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InvestmentPaymentStatusEventListener {

    private static final Logger log = LoggerFactory.getLogger(
            InvestmentPaymentStatusEventListener.class);

    private final InvestmentPaymentSettlementService settlementService;

    public InvestmentPaymentStatusEventListener(
            InvestmentPaymentSettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @KafkaListener(
            topics = "payment-status-events",
            groupId = "${spring.kafka.consumer.group-id:investment-service-group}"
    )
    public void consume(PaymentStatusEvent event) {
        if (event == null) {
            log.warn("Ignoring empty payment status event");
            return;
        }

        log.info("Received investment payment event: paymentId={}, investmentReference={}, transactionReference={}, businessType={}, status={}",
                event.getPaymentId(), event.getBusinessReference(),
                event.getBusinessTransactionReference(), event.getBusinessType(),
                event.getStatus());

        settlementService.handlePaymentStatusEvent(event);
    }
}
