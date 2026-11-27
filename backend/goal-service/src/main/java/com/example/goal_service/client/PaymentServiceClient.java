package com.example.goal_service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaymentServiceClient {

    private final RestClient restClient;

    public PaymentServiceClient(
            @Value("${services.payment.base-url:http://localhost:8085}")
            String paymentServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(paymentServiceUrl)
                .build();
    }

    public PaymentCreationResponse createGoalContributionPayment(
            Long userId,
            UUID contributionId,
            BigDecimal amount,
            String currency) {

        PaymentCreateRequest request = new PaymentCreateRequest(
                contributionId.toString(),
                "GOAL_CONTRIBUTION",
                amount,
                currency,
                "MOCK"
        );

        return restClient.post()
                .uri("/api/payments")
                .header("X-Authenticated-User-Id", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PaymentCreationResponse.class);
    }

    public record PaymentCreateRequest(
            String businessReference,
            String businessType,
            BigDecimal amount,
            String currency,
            String paymentMethod
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentCreationResponse(UUID id) {}
}
