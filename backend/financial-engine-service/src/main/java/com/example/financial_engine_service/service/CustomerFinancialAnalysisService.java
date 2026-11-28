package com.example.financial_engine_service.service;

import com.example.financial_engine_service.client.CustomerServiceClient;
import com.example.financial_engine_service.dto.CustomerDataMappingSummary;
import com.example.financial_engine_service.dto.CustomerFinancialDataResponse;
import com.example.financial_engine_service.dto.FinancialEngineAnalysisResponse;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CustomerFinancialAnalysisService {

    private final CustomerServiceClient customerServiceClient;
    private final CustomerFinancialSnapshotAssembler snapshotAssembler;
    private final FinancialEngineOrchestrationService orchestrationService;

    public CustomerFinancialAnalysisService(
            CustomerServiceClient customerServiceClient,
            CustomerFinancialSnapshotAssembler snapshotAssembler,
            FinancialEngineOrchestrationService orchestrationService) {
        this.customerServiceClient = customerServiceClient;
        this.snapshotAssembler = snapshotAssembler;
        this.orchestrationService = orchestrationService;
    }

    public CustomerAnalysisResult analyzeMyFinancials(
            Long userId, String email, String role, String currency) {

        String normalizedCurrency = currency.toUpperCase(Locale.ROOT);

        CustomerFinancialDataResponse customerData =
                customerServiceClient.getCustomerFinancialData(
                        userId, email, role);

        CustomerFinancialSnapshotAssembler.AssembledCustomerSnapshot assembled =
                snapshotAssembler.assemble(customerData, normalizedCurrency);

        FinancialEngineAnalysisResponse analysis = orchestrationService.analyze(
                userId, assembled.request());

        return new CustomerAnalysisResult(analysis, assembled.summary());
    }

    public record CustomerAnalysisResult(
            FinancialEngineAnalysisResponse analysis,
            CustomerDataMappingSummary mappingSummary) {
    }
}
