package com.example.financial_engine_service.client;

import com.example.financial_engine_service.dto.investment.InvestmentServiceInvestmentResponse;
import com.example.financial_engine_service.dto.investment.InvestmentServiceTransactionResponse;
import com.example.financial_engine_service.dto.investment.InvestmentValuationOverviewResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

/**
 * Read-only client for the existing Investment Service REST API.
 * Customer identity is forwarded through the trusted gateway header expected by the service.
 */
@Component
public class InvestmentServiceClient {

    private static final ParameterizedTypeReference<List<InvestmentServiceInvestmentResponse>>
            INVESTMENT_LIST_TYPE = new ParameterizedTypeReference<>() { };

    private static final ParameterizedTypeReference<List<InvestmentServiceTransactionResponse>>
            TRANSACTION_LIST_TYPE = new ParameterizedTypeReference<>() { };

    private final RestClient restClient;

    public InvestmentServiceClient(
            @Qualifier("investmentServiceRestClient") RestClient investmentServiceRestClient) {
        this.restClient = investmentServiceRestClient;
    }

    /** Returns all investment records belonging to the authenticated customer. */
    public List<InvestmentServiceInvestmentResponse> getMyInvestments(Long userId) {
        List<InvestmentServiceInvestmentResponse> response = restClient.get()
                .uri("/api/investments")
                .headers(headers -> addIdentityHeader(headers, userId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(INVESTMENT_LIST_TYPE);

        return response == null ? List.of() : response;
    }

    /** Returns transaction history for one customer-owned investment. */
    public List<InvestmentServiceTransactionResponse> getMyTransactions(
            Long userId, UUID investmentId) {
        List<InvestmentServiceTransactionResponse> response = restClient.get()
                .uri("/api/investments/{investmentId}/transactions", investmentId)
                .headers(headers -> addIdentityHeader(headers, userId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(TRANSACTION_LIST_TYPE);

        return response == null ? List.of() : response;
    }

    /**
     * Returns valuation and principal evidence for the authenticated customer.
     * Service errors intentionally propagate; an unavailable upstream must not be
     * silently represented as a genuinely empty portfolio.
     */
    public InvestmentValuationOverviewResponse getValuationOverview(Long userId) {
        InvestmentValuationOverviewResponse response = restClient.get()
                .uri("/api/investments/valuation-overview")
                .headers(headers -> addIdentityHeader(headers, userId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(InvestmentValuationOverviewResponse.class);

        return response == null ? InvestmentValuationOverviewResponse.empty() : response;
    }

    private void addIdentityHeader(HttpHeaders headers, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Authenticated user ID is required");
        }
        headers.set("X-Authenticated-User-Id", userId.toString());
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    }
}
