package com.example.financial_engine_service.client;

import com.example.financial_engine_service.dto.CustomerFinancialDataResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CustomerServiceClient {

    private final RestClient restClient;

    public CustomerServiceClient(RestClient customerServiceRestClient) {
        this.restClient = customerServiceRestClient;
    }

    public CustomerFinancialDataResponse getCustomerFinancialData(Long userId) {
        JsonNode profile = get(userId, "/api/customers/me");
        JsonNode employment = getOptional(userId, "/api/customers/me/employment");
        JsonNode incomeSources = getOptional(userId, "/api/customers/me/income-sources");
        JsonNode assets = getOptional(userId, "/api/customers/me/assets");
        JsonNode liabilities = getOptional(userId, "/api/customers/me/liabilities");
        JsonNode activeExpenses = getOptional(userId, "/api/customers/me/expenses/active");
        JsonNode riskProfile = getOptional(userId, "/api/customers/me/risk-profile");
        JsonNode household = getOptional(userId, "/api/customers/me/household");

        return new CustomerFinancialDataResponse(
                profile, employment, incomeSources, assets,
                liabilities, activeExpenses, riskProfile, household
        );
    }

    private JsonNode get(Long userId, String path) {
        return restClient.get()
                .uri(path)
                .headers(headers -> addIdentityHeader(headers, userId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(JsonNode.class);
    }

    private JsonNode getOptional(Long userId, String path) {
        try {
            return get(userId, path);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            return null;
        }
    }

    private void addIdentityHeader(HttpHeaders headers, Long userId) {
        headers.set("X-Authenticated-User-Id", userId.toString());
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
    }
}
