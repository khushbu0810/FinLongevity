
package com.example.financial_engine_service.dto.retirement;

import com.example.financial_engine_service.dto.debt.DebtRetirementAssessmentResponse;
import com.example.financial_engine_service.dto.investment.InvestmentEligibilityAssessment;
import com.example.financial_engine_service.dto.investment.InvestmentPrincipalEvidenceSummary;
import com.example.financial_engine_service.dto.investment.InvestmentValuationCurrencySummary;
import com.example.financial_engine_service.dto.investment.InvestmentValuationPrefillItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Read-only, reviewable prefill data assembled from
 * the customer's existing financial records.
 */
public record RetirementPlanningPrefillResponse(
        String currency,
        LocalDate asOfDate,
        Integer currentAge,
        BigDecimal currentRetirementCorpus,
        BigDecimal currentMonthlyIncome,
        BigDecimal currentMonthlyExpenses,
        List<RetirementExpenseInputSuggestion> expenseSuggestions,
        List<String> missingPlanningInputs,
        List<String> warnings,
        List<InvestmentEligibilityAssessment> investmentAssessments,
        List<InvestmentPrincipalEvidenceSummary> investmentPrincipalEvidenceByCurrency,
        List<InvestmentValuationPrefillItem> investmentValuations,
        List<InvestmentValuationCurrencySummary> investmentValuationSummaryByCurrency,
        DebtRetirementAssessmentResponse debtRetirementAssessment
) {

    public RetirementPlanningPrefillResponse {
        expenseSuggestions = expenseSuggestions == null
                ? List.of() : List.copyOf(expenseSuggestions);

        missingPlanningInputs = missingPlanningInputs == null
                ? List.of() : List.copyOf(missingPlanningInputs);

        warnings = warnings == null
                ? List.of() : List.copyOf(warnings);

        investmentAssessments = investmentAssessments == null
                ? List.of() : List.copyOf(investmentAssessments);

        investmentPrincipalEvidenceByCurrency =
                investmentPrincipalEvidenceByCurrency == null
                        ? List.of()
                        : List.copyOf(investmentPrincipalEvidenceByCurrency);

        investmentValuations = investmentValuations == null
                ? List.of() : List.copyOf(investmentValuations);

        investmentValuationSummaryByCurrency =
                investmentValuationSummaryByCurrency == null
                        ? List.of()
                        : List.copyOf(investmentValuationSummaryByCurrency);
    }

    /**
     * Backward-compatible constructor for the previous
     * investment valuation response.
     */
    public RetirementPlanningPrefillResponse(
            String currency,
            LocalDate asOfDate,
            Integer currentAge,
            BigDecimal currentRetirementCorpus,
            BigDecimal currentMonthlyIncome,
            BigDecimal currentMonthlyExpenses,
            List<RetirementExpenseInputSuggestion> expenseSuggestions,
            List<String> missingPlanningInputs,
            List<String> warnings,
            List<InvestmentEligibilityAssessment> investmentAssessments,
            List<InvestmentPrincipalEvidenceSummary> investmentPrincipalEvidenceByCurrency,
            List<InvestmentValuationPrefillItem> investmentValuations,
            List<InvestmentValuationCurrencySummary> investmentValuationSummaryByCurrency) {

        this(
                currency,
                asOfDate,
                currentAge,
                currentRetirementCorpus,
                currentMonthlyIncome,
                currentMonthlyExpenses,
                expenseSuggestions,
                missingPlanningInputs,
                warnings,
                investmentAssessments,
                investmentPrincipalEvidenceByCurrency,
                investmentValuations,
                investmentValuationSummaryByCurrency,
                null
        );
    }

    /**
     * Backward-compatible constructor for the pre-valuation response.
     */
    public RetirementPlanningPrefillResponse(
            String currency,
            LocalDate asOfDate,
            Integer currentAge,
            BigDecimal currentRetirementCorpus,
            BigDecimal currentMonthlyIncome,
            BigDecimal currentMonthlyExpenses,
            List<RetirementExpenseInputSuggestion> expenseSuggestions,
            List<String> missingPlanningInputs,
            List<String> warnings,
            List<InvestmentEligibilityAssessment> investmentAssessments,
            List<InvestmentPrincipalEvidenceSummary> investmentPrincipalEvidenceByCurrency) {

        this(
                currency,
                asOfDate,
                currentAge,
                currentRetirementCorpus,
                currentMonthlyIncome,
                currentMonthlyExpenses,
                expenseSuggestions,
                missingPlanningInputs,
                warnings,
                investmentAssessments,
                investmentPrincipalEvidenceByCurrency,
                List.of(),
                List.of(),
                null
        );
    }

    /**
     * Backward-compatible constructor for the original
     * customer-only prefill response.
     */
    public RetirementPlanningPrefillResponse(
            String currency,
            LocalDate asOfDate,
            Integer currentAge,
            BigDecimal currentRetirementCorpus,
            BigDecimal currentMonthlyIncome,
            BigDecimal currentMonthlyExpenses,
            List<RetirementExpenseInputSuggestion> expenseSuggestions,
            List<String> missingPlanningInputs,
            List<String> warnings) {

        this(
                currency,
                asOfDate,
                currentAge,
                currentRetirementCorpus,
                currentMonthlyIncome,
                currentMonthlyExpenses,
                expenseSuggestions,
                missingPlanningInputs,
                warnings,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null
        );
    }
}
