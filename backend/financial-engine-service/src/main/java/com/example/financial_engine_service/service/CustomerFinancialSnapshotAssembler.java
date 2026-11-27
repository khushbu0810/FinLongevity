package com.example.financial_engine_service.service;

import com.example.financial_engine_service.dto.CustomerDataMappingSummary;
import com.example.financial_engine_service.dto.CustomerFinancialDataResponse;
import com.example.financial_engine_service.dto.FinancialSnapshotRequest;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class CustomerFinancialSnapshotAssembler {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final BigDecimal FIFTY_TWO = BigDecimal.valueOf(52);
    private static final BigDecimal TWENTY_SIX = BigDecimal.valueOf(26);
    private static final BigDecimal THREE = BigDecimal.valueOf(3);

    public AssembledCustomerSnapshot assemble(
            CustomerFinancialDataResponse data, String requestedCurrency) {

        String currency = requestedCurrency.toUpperCase(Locale.ROOT);
        List<String> warnings = new ArrayList<>();

        BigDecimal monthlyIncome = sumIncome(data.incomeSources(), currency, warnings);
        BigDecimal monthlyExpenses = sumExpenses(data.activeExpenses(), currency, warnings);
        BigDecimal monthlyDebtPayments = sumMonthlyEmis(data.liabilities(), currency, warnings);
        BigDecimal totalAssets = sumAssets(data.assets(), currency, warnings);
        BigDecimal liquidAssets = sumLiquidAssets(data.assets(), currency, warnings);
        BigDecimal totalLiabilities = sumOutstandingLiabilities(data.liabilities(), currency, warnings);

        FinancialSnapshotRequest request = new FinancialSnapshotRequest(
                monthlyIncome,
                monthlyExpenses,
                monthlyDebtPayments,
                totalAssets,
                totalLiabilities,
                liquidAssets,
                currency
        );

        CustomerDataMappingSummary summary = new CustomerDataMappingSummary(
                currency, money(monthlyIncome), money(monthlyExpenses),
                money(monthlyDebtPayments), money(totalAssets),
                money(totalLiabilities), money(liquidAssets),
                countIncome(data.incomeSources(), currency),
                countExpenses(data.activeExpenses(), currency),
                countAssets(data.assets(), currency),
                countLiabilities(data.liabilities(), currency),
                List.copyOf(warnings)
        );

        return new AssembledCustomerSnapshot(request, summary);
    }

    private BigDecimal sumIncome(JsonNode rows, String currency, List<String> warnings) {
        BigDecimal total = ZERO;
        for (JsonNode row : elements(rows)) {
            if (row.has("active") && !row.path("active").asBoolean()) continue;
            if (!sameCurrency(row, currency)) continue;
            BigDecimal amount = decimal(row, "amount");
            BigDecimal monthly = toMonthly(amount, text(row, "frequency"), false);
            if (monthly == null) {
                warnings.add("An irregular income source was excluded from monthly income.");
                continue;
            }
            total = total.add(monthly);
        }
        return total;
    }

    private BigDecimal sumExpenses(JsonNode rows, String currency, List<String> warnings) {
        BigDecimal total = ZERO;
        for (JsonNode row : elements(rows)) {
            if (row.has("active") && !row.path("active").asBoolean()) continue;
            if (!sameCurrency(row, currency)) continue;
            String category = text(row, "category");
            // Loan EMI is sourced from LiabilityResponse.monthlyEmi to prevent double counting.
            if ("LOAN_EMI".equals(category)) continue;
            BigDecimal monthly = toMonthly(
                    decimal(row, "amount"), text(row, "frequency"), true);
            if (monthly == null) {
                warnings.add("A one-time or irregular expense was excluded from monthly expenses.");
                continue;
            }
            total = total.add(monthly);
        }
        return total;
    }

    private BigDecimal sumMonthlyEmis(JsonNode rows, String currency, List<String> warnings) {
        BigDecimal total = ZERO;
        for (JsonNode row : elements(rows)) {
            if (!sameCurrency(row, currency)) continue;
            JsonNode emi = row.get("monthlyEmi");
            if (emi != null && !emi.isNull()) total = total.add(emi.decimalValue());
        }
        return total;
    }

    private BigDecimal sumAssets(JsonNode rows, String currency, List<String> warnings) {
        BigDecimal total = ZERO;
        for (JsonNode row : elements(rows)) {
            if (!sameCurrency(row, currency)) continue;
            total = total.add(decimal(row, "currentValue"));
        }
        return total;
    }

    private BigDecimal sumLiquidAssets(JsonNode rows, String currency, List<String> warnings) {
        BigDecimal total = ZERO;
        for (JsonNode row : elements(rows)) {
            if (!sameCurrency(row, currency)) continue;
            if (row.path("liquid").asBoolean(false)) {
                total = total.add(decimal(row, "currentValue"));
            }
        }
        return total;
    }

    private BigDecimal sumOutstandingLiabilities(JsonNode rows, String currency,
                                                  List<String> warnings) {
        BigDecimal total = ZERO;
        for (JsonNode row : elements(rows)) {
            if (!sameCurrency(row, currency)) continue;
            total = total.add(decimal(row, "outstandingAmount"));
        }
        return total;
    }

    private BigDecimal toMonthly(BigDecimal amount, String frequency, boolean expense) {
        if (frequency == null) return null;
        return switch (frequency) {
            case "WEEKLY" -> amount.multiply(FIFTY_TWO).divide(TWELVE, 10, RoundingMode.HALF_UP);
            case "BIWEEKLY" -> amount.multiply(TWENTY_SIX).divide(TWELVE, 10, RoundingMode.HALF_UP);
            case "MONTHLY" -> amount;
            case "QUARTERLY" -> amount.divide(THREE, 10, RoundingMode.HALF_UP);
            case "ANNUALLY" -> amount.divide(TWELVE, 10, RoundingMode.HALF_UP);
            case "ONE_TIME", "IRREGULAR" -> null;
            default -> null;
        };
    }

    private boolean sameCurrency(JsonNode row, String currency) {
        return currency.equalsIgnoreCase(text(row, "currency"));
    }

    private String text(JsonNode row, String field) {
        JsonNode value = row.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private BigDecimal decimal(JsonNode row, String field) {
        JsonNode value = row.get(field);
        return value == null || value.isNull() ? ZERO : value.decimalValue();
    }

    private List<JsonNode> elements(JsonNode rows) {
        if (rows == null || rows.isNull() || !rows.isArray()) return List.of();
        List<JsonNode> result = new ArrayList<>();
        rows.forEach(result::add);
        return result;
    }

    private int countIncome(JsonNode rows, String currency) {
        return (int) elements(rows).stream()
                .filter(r -> (!r.has("active") || r.path("active").asBoolean())
                        && sameCurrency(r, currency)
                        && toMonthly(decimal(r, "amount"), text(r, "frequency"), false) != null)
                .count();
    }

    private int countExpenses(JsonNode rows, String currency) {
        return (int) elements(rows).stream()
                .filter(r -> (!r.has("active") || r.path("active").asBoolean())
                        && sameCurrency(r, currency)
                        && !"LOAN_EMI".equals(text(r, "category"))
                        && toMonthly(decimal(r, "amount"), text(r, "frequency"), true) != null)
                .count();
    }

    private int countAssets(JsonNode rows, String currency) {
        return (int) elements(rows).stream().filter(r -> sameCurrency(r, currency)).count();
    }

    private int countLiabilities(JsonNode rows, String currency) {
        return (int) elements(rows).stream().filter(r -> sameCurrency(r, currency)).count();
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public record AssembledCustomerSnapshot(
            FinancialSnapshotRequest request,
            CustomerDataMappingSummary summary) {
    }
}
