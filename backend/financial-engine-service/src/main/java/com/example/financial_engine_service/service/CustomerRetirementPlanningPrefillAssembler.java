package com.example.financial_engine_service.service;

import com.example.financial_engine_service.dto.CustomerFinancialDataResponse;
import com.example.financial_engine_service.dto.retirement.RetirementExpenseInputSuggestion;
import com.example.financial_engine_service.dto.retirement.RetirementPlanningPrefillResponse;
import com.example.financial_engine_service.enums.RetirementExpenseCategory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Maps existing customer records into editable retirement-planner input suggestions. */
@Component
public class CustomerRetirementPlanningPrefillAssembler {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final BigDecimal FIFTY_TWO = BigDecimal.valueOf(52);
    private static final BigDecimal TWENTY_SIX = BigDecimal.valueOf(26);
    private static final BigDecimal THREE = BigDecimal.valueOf(3);

    public RetirementPlanningPrefillResponse assemble(
            CustomerFinancialDataResponse data, String requestedCurrency, LocalDate asOfDate) {
        String currency = requestedCurrency.toUpperCase(Locale.ROOT);
        List<String> warnings = new ArrayList<>();

        Integer currentAge = calculateAge(data.profile(), asOfDate, warnings);
        BigDecimal retirementCorpus = calculateRetirementCorpus(data.assets(), currency, warnings);
        BigDecimal monthlyIncome = calculateMonthlyIncome(data.incomeSources(), currency, warnings);
        List<RetirementExpenseInputSuggestion> expenseSuggestions =
                mapExpenseSuggestions(data.activeExpenses(), currency, warnings);
        BigDecimal monthlyExpenses = expenseSuggestions.stream()
                .map(RetirementExpenseInputSuggestion::currentMonthlyAmount)
                .reduce(ZERO, BigDecimal::add);

        if (retirementCorpus.compareTo(ZERO) == 0) {
            warnings.add("No EPF, PPF or NPS assets were found in the selected currency; verify the retirement corpus manually.");
        }
        if (expenseSuggestions.isEmpty()) {
            warnings.add("No active recurring expenses were found in the selected currency.");
        }
        warnings.add("Only records already denominated in " + currency + " are included; no foreign-exchange conversion is performed.");
        warnings.add("Current expenses are suggestions, not confirmed retirement expenses. Review whether each expense will continue after retirement.");

        List<String> missingInputs = List.of(
                "RETIREMENT_AGE",
                "MONTHLY_RETIREMENT_CONTRIBUTION",
                "ANNUAL_EXPECTED_RETURN_PERCENTAGE",
                "ANNUAL_INFLATION_PERCENTAGE",
                "ANNUAL_CONTRIBUTION_GROWTH_PERCENTAGE",
                "CATEGORY_SPECIFIC_RETIREMENT_INFLATION",
                "RETIREMENT_INCOME_STREAMS",
                "RETIREMENT_PLANNING_AGE"
        );

        return new RetirementPlanningPrefillResponse(
                currency, asOfDate, currentAge, money(retirementCorpus), money(monthlyIncome),
                money(monthlyExpenses), expenseSuggestions, missingInputs, warnings);
    }

    private Integer calculateAge(JsonNode profile, LocalDate asOfDate, List<String> warnings) {
        String dob = text(profile, "dateOfBirth");
        if (dob == null || dob.isBlank()) {
            warnings.add("Date of birth is missing; current age cannot be prefilled.");
            return null;
        }
        try {
            LocalDate dateOfBirth = LocalDate.parse(dob);
            if (dateOfBirth.isAfter(asOfDate)) {
                warnings.add("Date of birth is in the future; current age cannot be prefilled.");
                return null;
            }
            return Period.between(dateOfBirth, asOfDate).getYears();
        } catch (RuntimeException ex) {
            warnings.add("Date of birth could not be parsed; current age cannot be prefilled.");
            return null;
        }
    }

    private BigDecimal calculateRetirementCorpus(JsonNode assets, String currency, List<String> warnings) {
        BigDecimal total = ZERO;
        for (JsonNode asset : elements(assets)) {
            String category = text(asset, "category");
            if (!currency.equalsIgnoreCase(text(asset, "currency"))) continue;
            if ("EPF".equals(category) || "PPF".equals(category) || "NPS".equals(category)) {
                total = total.add(decimal(asset, "currentValue"));
            }
        }
        return total;
    }

    private BigDecimal calculateMonthlyIncome(JsonNode rows, String currency, List<String> warnings) {
        BigDecimal total = ZERO;
        for (JsonNode row : elements(rows)) {
            if (row.has("active") && !row.path("active").asBoolean()) continue;
            if (!currency.equalsIgnoreCase(text(row, "currency"))) continue;
            BigDecimal monthly = monthlyAmount(decimal(row, "amount"), text(row, "frequency"));
            if (monthly == null) {
                warnings.add("An irregular income source was excluded from monthly income.");
                continue;
            }
            total = total.add(monthly);
        }
        return total;
    }

    private List<RetirementExpenseInputSuggestion> mapExpenseSuggestions(
            JsonNode rows, String currency, List<String> warnings) {
        List<RetirementExpenseInputSuggestion> result = new ArrayList<>();
        for (JsonNode row : elements(rows)) {
            if (row.has("active") && !row.path("active").asBoolean()) continue;
            if (!currency.equalsIgnoreCase(text(row, "currency"))) continue;
            String sourceCategory = text(row, "category");
            // Loan EMI is a liability repayment and is intentionally not treated as retirement consumption.
            if ("LOAN_EMI".equals(sourceCategory)) continue;
            BigDecimal monthly = monthlyAmount(decimal(row, "amount"), text(row, "frequency"));
            if (monthly == null) {
                warnings.add("A one-time or irregular expense was excluded from retirement expense suggestions.");
                continue;
            }
            boolean essential = row.path("essential").asBoolean(false);
            result.add(new RetirementExpenseInputSuggestion(
                    text(row, "id"), text(row, "name"), sourceCategory, text(row, "frequency"),
                    mapCategory(sourceCategory, essential), money(monthly), essential));
        }
        return List.copyOf(result);
    }

    private RetirementExpenseCategory mapCategory(String sourceCategory, boolean essential) {
        if ("HEALTHCARE".equals(sourceCategory)) return RetirementExpenseCategory.HEALTHCARE;
        if ("EDUCATION".equals(sourceCategory) || "CHILDCARE".equals(sourceCategory)) {
            return RetirementExpenseCategory.DEPENDENT_SUPPORT;
        }
        return essential ? RetirementExpenseCategory.ESSENTIAL : RetirementExpenseCategory.LIFESTYLE;
    }

    private BigDecimal monthlyAmount(BigDecimal amount, String frequency) {
        if (frequency == null) return null;
        return switch (frequency) {
            case "WEEKLY" -> amount.multiply(FIFTY_TWO).divide(TWELVE, 10, RoundingMode.HALF_UP);
            case "BIWEEKLY" -> amount.multiply(TWENTY_SIX).divide(TWELVE, 10, RoundingMode.HALF_UP);
            case "MONTHLY" -> amount;
            case "QUARTERLY" -> amount.divide(THREE, 10, RoundingMode.HALF_UP);
            case "ANNUALLY" -> amount.divide(TWELVE, 10, RoundingMode.HALF_UP);
            case "IRREGULAR", "ONE_TIME" -> null;
            default -> null;
        };
    }

    private List<JsonNode> elements(JsonNode rows) {
        if (rows == null || rows.isNull() || !rows.isArray()) return List.of();
        List<JsonNode> result = new ArrayList<>();
        rows.forEach(result::add);
        return result;
    }

    private String text(JsonNode node, String field) {
        if (node == null || node.isNull()) return null;
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private BigDecimal decimal(JsonNode node, String field) {
        if (node == null || node.isNull()) return ZERO;
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? ZERO : value.decimalValue();
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
