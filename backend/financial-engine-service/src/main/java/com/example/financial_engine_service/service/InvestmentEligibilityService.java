package com.example.financial_engine_service.service;

import com.example.financial_engine_service.client.InvestmentServiceClient;
import com.example.financial_engine_service.dto.investment.InvestmentEligibilityAssessment;
import com.example.financial_engine_service.dto.investment.InvestmentEligibilityResponse;
import com.example.financial_engine_service.dto.investment.InvestmentServiceInvestmentResponse;
import com.example.financial_engine_service.dto.investment.InvestmentServiceTransactionResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Applies conservative eligibility rules to the existing Investment Service records.
 * It calculates supported net principal evidence only; it never claims that this is
 * the current market value of the investment.
 */
@Service
public class InvestmentEligibilityService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final InvestmentServiceClient investmentServiceClient;

    public InvestmentEligibilityService(InvestmentServiceClient investmentServiceClient) {
        this.investmentServiceClient = investmentServiceClient;
    }

    public InvestmentEligibilityResponse assessCustomerInvestments(Long userId) {
        List<InvestmentServiceInvestmentResponse> investments =
                investmentServiceClient.getMyInvestments(userId);

        List<InvestmentEligibilityAssessment> assessments = investments.stream()
                .map(investment -> assessInvestment(userId, investment))
                .toList();

        int eligibleCount = (int) assessments.stream()
                .filter(InvestmentEligibilityAssessment::eligibleForPrincipalEvidence)
                .count();

        return new InvestmentEligibilityResponse(assessments.size(), eligibleCount, assessments);
    }

    private InvestmentEligibilityAssessment assessInvestment(
            Long userId, InvestmentServiceInvestmentResponse investment) {
        List<String> reasons = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        String status = normalize(investment.status());
        String currency = normalize(investment.currency());

        if (!"ACTIVE".equals(status) && !"PARTIALLY_REDEEMED".equals(status)) {
            reasons.add("Investment status is not ACTIVE or PARTIALLY_REDEEMED.");
        }
        if (currency == null || currency.isBlank()) {
            reasons.add("Investment currency is missing; transaction amounts cannot be safely combined.");
        }

        List<InvestmentServiceTransactionResponse> transactions = investmentServiceClient
                .getMyTransactions(userId, investment.id());

        BigDecimal netPrincipal = ZERO;
        int successfulCount = 0;
        for (InvestmentServiceTransactionResponse transaction : transactions) {
            if (!investment.id().equals(transaction.investmentId())) {
                warnings.add("A transaction linked to a different investment ID was ignored.");
                continue;
            }

            String transactionStatus = normalize(transaction.status());
            if (!"SUCCESS".equals(transactionStatus)) {
                if ("INITIATED".equals(transactionStatus) || "PENDING".equals(transactionStatus)) {
                    warnings.add("One or more transactions are still pending and were excluded.");
                }
                continue;
            }
            successfulCount++;

            String transactionCurrency = normalize(transaction.currency());
            if (currency == null || transactionCurrency == null || !currency.equals(transactionCurrency)) {
                warnings.add("A successful transaction with a missing or different currency was excluded.");
                continue;
            }
            if (transaction.amount() == null || transaction.amount().signum() <= 0) {
                warnings.add("A successful transaction with a missing or non-positive amount was excluded.");
                continue;
            }

            String type = normalize(transaction.transactionType());
            if ("PURCHASE".equals(type) || "ADDITIONAL_INVESTMENT".equals(type)) {
                netPrincipal = netPrincipal.add(transaction.amount());
            } else if ("PARTIAL_REDEMPTION".equals(type) || "FULL_REDEMPTION".equals(type)) {
                netPrincipal = netPrincipal.subtract(transaction.amount());
            } else if (type == null || !List.of("INTEREST_CREDIT", "DIVIDEND_CREDIT", "FEE", "REVERSAL").contains(type)) {
                warnings.add("An unrecognized successful transaction type was excluded.");
            } else {
                warnings.add(type + " transaction was not included in principal evidence.");
            }
        }

        if (successfulCount == 0) {
            reasons.add("No SUCCESS investment transactions were found.");
        }
        if (netPrincipal.signum() < 0) {
            warnings.add("Successful redemptions exceed supported contributions; supported principal was floored at zero.");
            netPrincipal = ZERO;
        }
        if (netPrincipal.signum() == 0) {
            reasons.add("No positive net contribution principal is supported by successful transactions.");
        }

        boolean eligible = reasons.isEmpty() && netPrincipal.signum() > 0;
        if (eligible) {
            reasons.add("Eligible as historical net principal evidence only; not as current market value.");
        }

        return new InvestmentEligibilityAssessment(
                investment.id(), investment.productNameSnapshot(), status, currency,
                eligible, eligible ? netPrincipal : ZERO, transactions.size(), successfulCount,
                false, reasons, warnings
        );
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
