package com.example.financial_engine_service.client;

import com.example.financial_engine_service.dto.CustomerFinancialDataResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

@Component
public class CustomerServiceClient {

    private final RestClient restClient;

    public CustomerServiceClient(
            @Qualifier("customerServiceRestClient") RestClient customerServiceRestClient) {
        this.restClient = customerServiceRestClient;
    }

    public CustomerFinancialDataResponse getCustomerFinancialData(Long userId, String email, String role) {

        JsonNode profile = get(userId, email, role, "/api/customers/me");
        JsonNode employment = getOptional(userId, email, role, "/api/customers/me/employment");
        JsonNode incomeSources = getOptional(userId, email, role, "/api/customers/me/income-sources");
        JsonNode assets = getOptional(userId, email, role, "/api/customers/me/assets");
        JsonNode liabilities = getOptional(userId, email, role, "/api/customers/me/liabilities");
        JsonNode activeExpenses = getOptional(userId, email, role, "/api/customers/me/expenses/active");
        JsonNode riskProfile = getOptional(userId, email, role, "/api/customers/me/risk-profile");
        JsonNode household = getOptional(userId, email, role, "/api/customers/me/household");

        return new CustomerFinancialDataResponse(
                profile, employment, incomeSources, assets,
                liabilities, activeExpenses, riskProfile, household
        );
    }

    private JsonNode get(Long userId, String email, String role, String path) {
        return restClient.get()
                .uri(path)
                .headers(headers -> addIdentityHeaders(headers, userId, email, role))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(JsonNode.class);
    }

    private JsonNode getOptional(Long userId, String email, String role, String path) {
        try {
            return get(userId, email, role, path);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            return null;
        }
    }

    private void addIdentityHeaders(HttpHeaders headers, Long userId, String email, String role) {
        headers.set("X-Authenticated-User-Id", userId.toString());
        headers.set("X-Authenticated-User-Email", email);
        headers.set("X-Authenticated-User-Role", role);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
    }
}
