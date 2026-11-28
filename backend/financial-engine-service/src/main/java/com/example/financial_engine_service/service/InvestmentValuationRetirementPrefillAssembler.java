package com.example.financial_engine_service.service;

import com.example.financial_engine_service.dto.investment.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/** Converts latest valuation records into conservative, currency-separated retirement evidence. */
@Component
public class InvestmentValuationRetirementPrefillAssembler {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    public InvestmentValuationPrefillResult assemble(
            InvestmentValuationOverviewResponse overview,
            LocalDate asOfDate,
            long maximumAgeDays) {
        if (maximumAgeDays < 0) {
            throw new IllegalArgumentException("Maximum valuation age cannot be negative");
        }
        if (overview == null || overview.investments().isEmpty()) {
            return InvestmentValuationPrefillResult.empty();
        }

        Map<String, CurrencyAccumulator> byCurrency = new TreeMap<>();
        List<InvestmentValuationPrefillItem> items = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (InvestmentValuationOverviewItemResponse investment : overview.investments()) {
            if (investment == null) continue;
            String currency = normalizeCurrency(investment.currency());
            CurrencyAccumulator accumulator = currency == null
                    ? null : byCurrency.computeIfAbsent(currency, ignored -> new CurrencyAccumulator());
            if (accumulator != null) accumulator.investmentCount++;

            List<String> itemWarnings = new ArrayList<>();
            InvestmentValuationResponse valuation = investment.latestValuation();
            BigDecimal recordedValue = valuation == null ? null : valuation.marketValue();
            LocalDate valuationDate = valuation == null ? null : valuation.valuationDate();
            String source = valuation == null ? null : valuation.valuationSource();
            Long ageDays = valuationDate == null ? null
                    : Math.max(0L, java.time.temporal.ChronoUnit.DAYS.between(valuationDate, asOfDate));

            String status;
            boolean stale = false;
            BigDecimal usableValue = null;
            BigDecimal gainLoss = null;

            if (currency == null) {
                status = "CURRENCY_MISSING";
                itemWarnings.add("Investment currency is missing; its valuation cannot be safely grouped or used.");
            } else if (valuation == null || recordedValue == null || valuationDate == null) {
                status = "UNAVAILABLE";
                if (accumulator != null) accumulator.unavailableValuationCount++;
                itemWarnings.add("No complete valuation is available for this investment.");
            } else if (valuation.currency() == null
                    || !currency.equals(normalizeCurrency(valuation.currency()))) {
                status = "CURRENCY_MISMATCH";
                if (accumulator != null) accumulator.unavailableValuationCount++;
                itemWarnings.add("Valuation currency does not match the parent investment currency; the value was excluded.");
            } else if (recordedValue.signum() < 0) {
                status = "INVALID_VALUE";
                if (accumulator != null) accumulator.unavailableValuationCount++;
                itemWarnings.add("Negative market value was rejected from retirement prefill evidence.");
            } else {
                stale = ageDays != null && ageDays > maximumAgeDays;
                if (stale) {
                    status = "STALE";
                    if (accumulator != null) accumulator.staleValuationCount++;
                    itemWarnings.add("Latest valuation is older than the configured maximum age of "
                            + maximumAgeDays + " days; it is shown for reference but not treated as current value.");
                } else {
                    status = "AVAILABLE";
                    usableValue = recordedValue;
                    if (accumulator != null) {
                        accumulator.availableValuationCount++;
                        accumulator.freshMarketValueTotal = accumulator.freshMarketValueTotal.add(recordedValue);
                    }
                    if (investment.netInvestedPrincipal() != null
                            && investment.netInvestedPrincipal().signum() >= 0) {
                        gainLoss = recordedValue.subtract(investment.netInvestedPrincipal());
                    } else {
                        itemWarnings.add("Unrealized gain/loss is unavailable because comparable net principal evidence is missing.");
                    }
                }
            }

            if (currency == null) {
                warnings.add("At least one investment has no currency and was excluded from currency summaries.");
            }
            warnings.addAll(itemWarnings);
            items.add(new InvestmentValuationPrefillItem(
                    investment.investmentId(),
                    investment.productNameSnapshot(),
                    investment.investmentStatus(),
                    currency,
                    investment.netInvestedPrincipal(),
                    recordedValue,
                    usableValue,
                    gainLoss,
                    valuationDate,
                    source,
                    ageDays,
                    status,
                    stale,
                    false,
                    itemWarnings
            ));
        }

        List<InvestmentValuationCurrencySummary> summaries = new ArrayList<>();
        byCurrency.forEach((currency, accumulator) -> summaries.add(
                new InvestmentValuationCurrencySummary(
                        currency,
                        accumulator.investmentCount,
                        accumulator.availableValuationCount,
                        accumulator.staleValuationCount,
                        accumulator.unavailableValuationCount,
                        accumulator.freshMarketValueTotal,
                        false
                )));

        if (!items.isEmpty()) {
            warnings.add("Investment valuations are shown as separate evidence and are not added to current retirement corpus. Reconcile them against Customer Service assets before manually including them to avoid double-counting.");
        }
        if (items.stream().anyMatch(item -> item.currency() != null
                && items.stream().anyMatch(other -> other.currency() != null
                && !item.currency().equals(other.currency())))) {
            warnings.add("Investment valuation totals are separated by currency; no foreign-exchange conversion is performed.");
        }
        return new InvestmentValuationPrefillResult(items, summaries, warnings.stream().distinct().toList());
    }

    private String normalizeCurrency(String currency) {
        return currency == null || currency.isBlank()
                ? null : currency.trim().toUpperCase(Locale.ROOT);
    }

    private static final class CurrencyAccumulator {
        private int investmentCount;
        private int availableValuationCount;
        private int staleValuationCount;
        private int unavailableValuationCount;
        private BigDecimal freshMarketValueTotal = ZERO;
    }
}
