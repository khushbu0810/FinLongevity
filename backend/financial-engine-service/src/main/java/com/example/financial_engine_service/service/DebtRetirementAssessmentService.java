package com.example.financial_engine_service.service;

import com.example.financial_engine_service.dto.debt.*;
import com.example.financial_engine_service.engine.DebtRepaymentCalculator;
import com.example.financial_engine_service.enums.DebtRepaymentCalculationStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Read-only retirement assessment over liabilities already owned by Customer Service.
 * Estimates use only fields currently present on Customer Service LiabilityResponse.
 */
@Service
public class DebtRetirementAssessmentService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final int MAX_PROJECTION_MONTHS = 1_800;

    private final DebtRepaymentCalculator repaymentCalculator = new DebtRepaymentCalculator();

    public DebtRetirementAssessmentResponse assess(
            JsonNode liabilities,
            JsonNode customerProfile,
            String requestedCurrency,
            Integer currentAge,
            Integer plannedRetirementAge,
            LocalDate asOfDate) {

        String currency = requestedCurrency.toUpperCase(Locale.ROOT);
        List<String> warnings = new ArrayList<>();
        LocalDate retirementDate = resolveRetirementDate(
                customerProfile, currentAge, plannedRetirementAge, asOfDate, warnings);
        boolean retirementAgeAvailable = currentAge != null && plannedRetirementAge != null
                && retirementDate != null;

        BigDecimal totalOutstanding = ZERO;
        BigDecimal totalMonthlyEmi = ZERO;
        BigDecimal estimatedAtRetirement = ZERO;
        int totalCount = 0;
        int assessedCount = 0;
        int continuingCount = 0;
        int paidOffCount = 0;
        int nonAmortizingCount = 0;
        int insufficientCount = 0;
        int otherCurrencyCount = 0;
        List<DebtRetirementLiabilityAssessment> assessments = new ArrayList<>();

        for (JsonNode liability : elements(liabilities)) {
            String liabilityCurrency = text(liability, "currency");
            if (liabilityCurrency == null || liabilityCurrency.isBlank()) {
                warnings.add("A liability without a currency was excluded from the " + currency + " debt assessment.");
                continue;
            }
            if (!currency.equalsIgnoreCase(liabilityCurrency)) {
                otherCurrencyCount++;
                continue;
            }

            totalCount++;
            BigDecimal outstanding = decimal(liability, "outstandingAmount");
            BigDecimal emi = decimal(liability, "monthlyEmi");
            if (outstanding != null && outstanding.signum() >= 0) {
                totalOutstanding = totalOutstanding.add(money(outstanding));
            } else {
                warnings.add("Liability " + safeName(liability)
                        + " has a missing or invalid outstanding amount; it is excluded from the outstanding total.");
            }
            if (emi != null && emi.signum() >= 0) {
                totalMonthlyEmi = totalMonthlyEmi.add(money(emi));
            } else if (emi != null) {
                warnings.add("Liability " + safeName(liability)
                        + " has a negative EMI; it was excluded from the EMI total.");
            }

            BigDecimal rate = decimal(liability, "annualInterestRate");
            List<String> itemWarnings = new ArrayList<>();
            LocalDate nextPaymentDate = date(liability, "nextPaymentDate");
            String rawNextPaymentDate = text(liability, "nextPaymentDate");
            if (rawNextPaymentDate != null && !rawNextPaymentDate.isBlank() && nextPaymentDate == null) {
                itemWarnings.add("Next payment date is invalid; calendar payoff date cannot be estimated.");
            }
            UUID liabilityId = uuid(liability, itemWarnings);
            String name = text(liability, "name");
            String category = text(liability, "category");

            Integer paymentCountToRetirement = null;
            if (retirementAgeAvailable) {
                paymentCountToRetirement = estimatePaymentCount(
                        nextPaymentDate, asOfDate, retirementDate, itemWarnings);
                if (paymentCountToRetirement > MAX_PROJECTION_MONTHS) {
                    itemWarnings.add("Retirement horizon exceeds the supported debt projection limit of 1,800 months.");
                    paymentCountToRetirement = null;
                }
            }

            DebtRepaymentProjection projection = repaymentCalculator.calculate(
                    outstanding, rate, emi, nextPaymentDate, asOfDate, paymentCountToRetirement);
            itemWarnings.addAll(projection.warnings());

            DebtRetirementAssessmentStatus status = mapStatus(
                    projection, retirementAgeAvailable, paymentCountToRetirement);
            BigDecimal balanceAtRetirement = retirementAgeAvailable
                    ? projection.projectedBalanceAfterMonths() : null;
            if (status == DebtRetirementAssessmentStatus.PAID_OFF
                    || status == DebtRetirementAssessmentStatus.ESTIMATED_PAID_OFF_BEFORE_RETIREMENT) {
                balanceAtRetirement = ZERO;
            }
            if (retirementAgeAvailable && paymentCountToRetirement == null
                    && projection.calculationStatus() != DebtRepaymentCalculationStatus.PAID_OFF
                    && projection.calculationStatus() != DebtRepaymentCalculationStatus.INSUFFICIENT_DATA) {
                status = DebtRetirementAssessmentStatus.INSUFFICIENT_DATA;
                balanceAtRetirement = null;
            }

            Integer overlapMonths = calculateOverlapMonths(
                    projection, paymentCountToRetirement, status);

            if (status == DebtRetirementAssessmentStatus.PAID_OFF
                    || status == DebtRetirementAssessmentStatus.ESTIMATED_PAID_OFF_BEFORE_RETIREMENT) {
                paidOffCount++;
            }
            if (status == DebtRetirementAssessmentStatus.ESTIMATED_CONTINUES_INTO_RETIREMENT) {
                continuingCount++;
            }
            if (status == DebtRetirementAssessmentStatus.NON_AMORTIZING) {
                nonAmortizingCount++;
            }
            if (status == DebtRetirementAssessmentStatus.INSUFFICIENT_DATA) {
                insufficientCount++;
            }
            if (balanceAtRetirement != null) {
                assessedCount++;
                estimatedAtRetirement = estimatedAtRetirement.add(money(balanceAtRetirement));
            }

            assessments.add(new DebtRetirementLiabilityAssessment(
                    liabilityId, name, category,
                    outstanding == null ? null : money(outstanding), currency,
                    rate, emi == null ? null : money(emi), nextPaymentDate,
                    projection.estimatedMonthsToPayoff(), projection.estimatedPayoffDate(),
                    balanceAtRetirement == null ? null : money(balanceAtRetirement),
                    overlapMonths, status, itemWarnings));
        }

        if (otherCurrencyCount > 0) {
            warnings.add(otherCurrencyCount + " liability record(s) in other currencies were excluded. No foreign-exchange conversion was performed.");
        }
        if (totalCount == 0) {
            warnings.add("No liabilities were found in " + currency + " for this customer.");
        } else {
            warnings.add("Customer Service does not currently expose an active/closed liability status; all returned records are treated as current obligations.");
            warnings.add("Loan EMI is reported separately and remains excluded from ordinary monthly expense suggestions to avoid double counting.");
            warnings.add("Estimated debt at retirement is informational and is not automatically deducted from current retirement corpus.");
        }
        if (!retirementAgeAvailable) {
            warnings.add("Provide plannedRetirementAge and a usable current age to assess which liabilities may continue into retirement.");
        }
        if (insufficientCount > 0) {
            warnings.add(insufficientCount + " liability record(s) lack sufficient data or exceed the supported horizon for a defensible retirement estimate.");
        }
        if (nonAmortizingCount > 0) {
            warnings.add(nonAmortizingCount + " liability record(s) are non-amortizing under the current EMI and interest assumptions.");
        }

        DebtRetirementAssessmentSummary summary = new DebtRetirementAssessmentSummary(
                currency, money(totalOutstanding), money(totalMonthlyEmi),
                money(estimatedAtRetirement), totalCount, assessedCount,
                continuingCount, paidOffCount, nonAmortizingCount, insufficientCount);

        return new DebtRetirementAssessmentResponse(
                currency, asOfDate, currentAge, plannedRetirementAge, retirementDate,
                summary, assessments, warnings.stream().distinct().toList());
    }

    private DebtRetirementAssessmentStatus mapStatus(
            DebtRepaymentProjection projection,
            boolean retirementAgeAvailable,
            Integer paymentCountToRetirement) {
        DebtRepaymentCalculationStatus calculationStatus = projection.calculationStatus();
        if (calculationStatus == DebtRepaymentCalculationStatus.PAID_OFF) {
            return DebtRetirementAssessmentStatus.PAID_OFF;
        }
        if (calculationStatus == DebtRepaymentCalculationStatus.INSUFFICIENT_DATA) {
            return DebtRetirementAssessmentStatus.INSUFFICIENT_DATA;
        }
        if (calculationStatus == DebtRepaymentCalculationStatus.NON_AMORTIZING) {
            return DebtRetirementAssessmentStatus.NON_AMORTIZING;
        }
        if (!retirementAgeAvailable) {
            return DebtRetirementAssessmentStatus.RETIREMENT_AGE_NOT_PROVIDED;
        }
        if (paymentCountToRetirement == null) {
            return DebtRetirementAssessmentStatus.INSUFFICIENT_DATA;
        }

        BigDecimal balanceAtRetirement = projection.projectedBalanceAfterMonths();
        if (balanceAtRetirement != null) {
            return balanceAtRetirement.signum() == 0
                    ? DebtRetirementAssessmentStatus.ESTIMATED_PAID_OFF_BEFORE_RETIREMENT
                    : DebtRetirementAssessmentStatus.ESTIMATED_CONTINUES_INTO_RETIREMENT;
        }
        if (projection.calculationStatus() == DebtRepaymentCalculationStatus.MAXIMUM_TERM_EXCEEDED) {
            return DebtRetirementAssessmentStatus.INSUFFICIENT_DATA;
        }
        Integer monthsToPayoff = projection.estimatedMonthsToPayoff();
        if (monthsToPayoff != null) {
            return monthsToPayoff <= paymentCountToRetirement
                    ? DebtRetirementAssessmentStatus.ESTIMATED_PAID_OFF_BEFORE_RETIREMENT
                    : DebtRetirementAssessmentStatus.ESTIMATED_CONTINUES_INTO_RETIREMENT;
        }
        return DebtRetirementAssessmentStatus.INSUFFICIENT_DATA;
    }

    private Integer calculateOverlapMonths(
            DebtRepaymentProjection projection,
            Integer paymentCountToRetirement,
            DebtRetirementAssessmentStatus status) {
        if (status == DebtRetirementAssessmentStatus.PAID_OFF
                || status == DebtRetirementAssessmentStatus.ESTIMATED_PAID_OFF_BEFORE_RETIREMENT) {
            return 0;
        }
        if (status == DebtRetirementAssessmentStatus.NON_AMORTIZING
                || status == DebtRetirementAssessmentStatus.INSUFFICIENT_DATA
                || status == DebtRetirementAssessmentStatus.RETIREMENT_AGE_NOT_PROVIDED
                || paymentCountToRetirement == null
                || projection.estimatedMonthsToPayoff() == null) {
            return null;
        }
        return Math.max(0, projection.estimatedMonthsToPayoff() - paymentCountToRetirement);
    }

    private LocalDate resolveRetirementDate(
            JsonNode profile,
            Integer currentAge,
            Integer plannedRetirementAge,
            LocalDate asOfDate,
            List<String> warnings) {
        if (currentAge == null || plannedRetirementAge == null) return null;
        if (currentAge < 0 || currentAge > 120 || plannedRetirementAge < 1 || plannedRetirementAge > 120) {
            warnings.add("Current age or planned retirement age is outside the supported range; retirement overlap was not estimated.");
            return null;
        }
        if (plannedRetirementAge < currentAge) {
            warnings.add("The planned retirement age is below the customer's current age; debt overlap is assessed from the current date.");
            return asOfDate;
        }

        String dobText = text(profile, "dateOfBirth");
        LocalDate candidate = null;
        if (dobText != null && !dobText.isBlank()) {
            try {
                candidate = LocalDate.parse(dobText).plusYears(plannedRetirementAge);
            } catch (RuntimeException ignored) {
                warnings.add("Date of birth could not be parsed; retirement date is approximated from current age.");
            }
        }
        if (candidate == null) {
            warnings.add("Exact date of birth is unavailable; retirement date is approximated from current age and planned retirement age.");
            candidate = asOfDate.plusYears(Math.max(0, plannedRetirementAge - currentAge));
        }
        if (candidate.isBefore(asOfDate)) {
            warnings.add("The planned retirement age has already been reached; debt overlap is assessed from the current date.");
            return asOfDate;
        }
        return candidate;
    }

    /** Counts monthly instalments scheduled on or before the retirement date. */
    private int estimatePaymentCount(
            LocalDate nextPaymentDate,
            LocalDate asOfDate,
            LocalDate retirementDate,
            List<String> warnings) {
        if (retirementDate == null || !retirementDate.isAfter(asOfDate)) return 0;
        if (nextPaymentDate == null) {
            warnings.add("Next payment date is missing; retirement balance uses an approximate monthly payment count from the as-of date.");
            return boundedMonthDifference(asOfDate, retirementDate);
        }
        if (nextPaymentDate.isBefore(asOfDate)) {
            warnings.add("Next payment date is before the as-of date; retirement balance uses an approximate monthly payment count from the as-of date and does not model arrears.");
            return boundedMonthDifference(asOfDate, retirementDate);
        }
        if (nextPaymentDate.isAfter(retirementDate)) return 0;

        int count = 0;
        LocalDate paymentDate = nextPaymentDate;
        while (!paymentDate.isAfter(retirementDate) && count <= MAX_PROJECTION_MONTHS) {
            count++;
            paymentDate = paymentDate.plusMonths(1);
        }
        return count;
    }

    private int boundedMonthDifference(LocalDate from, LocalDate to) {
        long months = ChronoUnit.MONTHS.between(from, to);
        return (int) Math.min(Math.max(months, 0L), MAX_PROJECTION_MONTHS + 1L);
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
        if (node == null || node.isNull()) return null;
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.asText().isBlank()) return null;
        try {
            return value.isNumber() ? value.decimalValue() : new BigDecimal(value.asText());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private LocalDate date(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private UUID uuid(JsonNode node, List<String> warnings) {
        String value = text(node, "id");
        if (value == null || value.isBlank()) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            warnings.add("The liability identifier could not be parsed as a UUID.");
            return null;
        }
    }

    private String safeName(JsonNode node) {
        String name = text(node, "name");
        return name == null || name.isBlank() ? "unnamed liability" : name;
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }
}
