package com.example.financial_engine_service.service;

import com.example.financial_engine_service.client.CustomerServiceClient;
import com.example.financial_engine_service.client.InvestmentServiceClient;
import com.example.financial_engine_service.dto.CustomerFinancialDataResponse;
import com.example.financial_engine_service.dto.investment.InvestmentEligibilityResponse;
import com.example.financial_engine_service.dto.investment.InvestmentValuationOverviewResponse;
import com.example.financial_engine_service.dto.investment.InvestmentValuationPrefillResult;
import com.example.financial_engine_service.dto.retirement.RetirementPlanningPrefillResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class CustomerRetirementPlanningPrefillService {

    private static final long DEFAULT_MAXIMUM_VALUATION_AGE_DAYS = 90L;
    private static final String INVESTMENT_EVIDENCE_WARNING =
            "Investment net principal is historical transaction evidence, not current market value, " +
            "and is not included in current retirement corpus. It may overlap with manually recorded " +
            "assets; reconcile records before using it in a retirement calculation.";

    private final CustomerServiceClient customerServiceClient;
    private final CustomerRetirementPlanningPrefillAssembler assembler;
    private final InvestmentEligibilityService investmentEligibilityService;
    private final InvestmentRetirementPrefillAssembler investmentPrefillAssembler;
    private final InvestmentServiceClient investmentServiceClient;
    private final InvestmentValuationRetirementPrefillAssembler valuationPrefillAssembler;

    public CustomerRetirementPlanningPrefillService(
            CustomerServiceClient customerServiceClient,
            CustomerRetirementPlanningPrefillAssembler assembler,
            InvestmentEligibilityService investmentEligibilityService,
            InvestmentRetirementPrefillAssembler investmentPrefillAssembler,
            InvestmentServiceClient investmentServiceClient,
            InvestmentValuationRetirementPrefillAssembler valuationPrefillAssembler) {
        this.customerServiceClient = customerServiceClient;
        this.assembler = assembler;
        this.investmentEligibilityService = investmentEligibilityService;
        this.investmentPrefillAssembler = investmentPrefillAssembler;
        this.investmentServiceClient = investmentServiceClient;
        this.valuationPrefillAssembler = valuationPrefillAssembler;
    }

    /** Backward-compatible entry point retaining the documented 90-day default freshness window. */
    public RetirementPlanningPrefillResponse getMyRetirementPlanningPrefill(
            Long userId, String email, String role, String currency) {
        return getMyRetirementPlanningPrefill(
                userId, email, role, currency, DEFAULT_MAXIMUM_VALUATION_AGE_DAYS);
    }

    public RetirementPlanningPrefillResponse getMyRetirementPlanningPrefill(
            Long userId, String email, String role, String currency, long maximumValuationAgeDays) {
        if (maximumValuationAgeDays < 0 || maximumValuationAgeDays > 3650) {
            throw new IllegalArgumentException("Maximum valuation age must be between 0 and 3650 days");
        }
        String normalizedCurrency = currency.toUpperCase(Locale.ROOT);
        LocalDate asOfDate = LocalDate.now();
        CustomerFinancialDataResponse data = customerServiceClient.getCustomerFinancialData(
                userId, email, role);
        RetirementPlanningPrefillResponse customerPrefill =
                assembler.assemble(data, normalizedCurrency, asOfDate);

        List<String> additionalWarnings = new ArrayList<>();
        InvestmentEligibilityResponse investmentResponse;
        try {
            investmentResponse = investmentEligibilityService.assessCustomerInvestments(userId);
        } catch (RestClientException ex) {
            investmentResponse = new InvestmentEligibilityResponse(0, 0, List.of());
            additionalWarnings.add(
                    "Investment Service could not be reached for transaction evidence; investment assessments were not included in this prefill.");
        }

        InvestmentValuationOverviewResponse valuationOverview;
        try {
            valuationOverview = investmentServiceClient.getValuationOverview(userId);
        } catch (RestClientException ex) {
            valuationOverview = InvestmentValuationOverviewResponse.empty();
            additionalWarnings.add(
                    "Investment Service valuation overview could not be reached; current investment valuations were not included in this prefill.");
        }

        InvestmentValuationPrefillResult valuationResult = valuationPrefillAssembler.assemble(
                valuationOverview, asOfDate, maximumValuationAgeDays);

        List<String> warnings = new ArrayList<>(customerPrefill.warnings());
        warnings.addAll(additionalWarnings);
        warnings.addAll(valuationResult.warnings());
        if (!investmentResponse.investments().isEmpty()) {
            warnings.add(INVESTMENT_EVIDENCE_WARNING);
        }
        if (investmentResponse.investments().stream()
                .anyMatch(item -> item.currency() != null
                        && !normalizedCurrency.equalsIgnoreCase(item.currency()))) {
            warnings.add("Investment principal evidence in other currencies is shown separately; no FX conversion is performed.");
        }
        if (valuationResult.investments().stream()
                .anyMatch(item -> item.currency() != null
                        && !normalizedCurrency.equalsIgnoreCase(item.currency()))) {
            warnings.add("Investment valuations in currencies other than " + normalizedCurrency
                    + " are returned in separate currency summaries; no FX conversion is performed.");
        }

        return new RetirementPlanningPrefillResponse(
                customerPrefill.currency(),
                customerPrefill.asOfDate(),
                customerPrefill.currentAge(),
                customerPrefill.currentRetirementCorpus(),
                customerPrefill.currentMonthlyIncome(),
                customerPrefill.currentMonthlyExpenses(),
                customerPrefill.expenseSuggestions(),
                customerPrefill.missingPlanningInputs(),
                warnings.stream().distinct().toList(),
                investmentResponse.investments(),
                investmentPrefillAssembler.summarize(investmentResponse),
                valuationResult.investments(),
                valuationResult.summariesByCurrency()
        );
    }
}
