package com.example.financial_engine_service.service;

import com.example.financial_engine_service.dto.investment.InvestmentEligibilityAssessment;
import com.example.financial_engine_service.dto.investment.InvestmentEligibilityResponse;
import com.example.financial_engine_service.dto.investment.InvestmentPrincipalEvidenceSummary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Converts eligibility assessments into currency-separated retirement-prefill evidence. */
@Component
public class InvestmentRetirementPrefillAssembler {

    public List<InvestmentPrincipalEvidenceSummary> summarize(
            InvestmentEligibilityResponse eligibilityResponse) {
        if (eligibilityResponse == null || eligibilityResponse.investments().isEmpty()) {
            return List.of();
        }

        Map<String, CurrencyAccumulator> totalsByCurrency = new TreeMap<>();
        for (InvestmentEligibilityAssessment assessment : eligibilityResponse.investments()) {
            if (assessment == null || !assessment.eligibleForPrincipalEvidence()) {
                continue;
            }
            String currency = normalizeCurrency(assessment.currency());
            BigDecimal principal = assessment.supportedNetPrincipal();
            if (currency == null || principal == null || principal.signum() <= 0) {
                continue;
            }
            totalsByCurrency.computeIfAbsent(currency, ignored -> new CurrencyAccumulator())
                    .add(principal);
        }

        List<InvestmentPrincipalEvidenceSummary> summaries = new ArrayList<>();
        totalsByCurrency.forEach((currency, accumulator) -> summaries.add(
                new InvestmentPrincipalEvidenceSummary(
                        currency,
                        accumulator.count,
                        accumulator.total,
                        false,
                        false
                )));
        return List.copyOf(summaries);
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) return null;
        return currency.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private static final class CurrencyAccumulator {
        private int count;
        private BigDecimal total = BigDecimal.ZERO;

        private void add(BigDecimal amount) {
            count++;
            total = total.add(amount);
        }
    }
}
