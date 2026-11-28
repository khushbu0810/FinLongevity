package com.example.financial_engine_service.engine;

import com.example.financial_engine_service.dto.retirement.RetirementIncomeStreamProjectionResponse;
import com.example.financial_engine_service.dto.retirement.RetirementIncomeStreamRequest;
import com.example.financial_engine_service.enums.RetirementIncomeFrequency;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/** Calculates recurring retirement income in monthly equivalents. */
@Component
public class RetirementIncomeCalculator {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal THREE = BigDecimal.valueOf(3);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);

    public BigDecimal calculateMonthlyIncomeAtAge(
            List<RetirementIncomeStreamRequest> streams, int currentAge, int targetAge) {
        Objects.requireNonNull(streams, "streams must not be null");
        return streams.stream()
                .filter(stream -> isActiveAtAge(stream, targetAge))
                .map(stream -> projectMonthlyAmount(stream, currentAge, targetAge))
                .reduce(BigDecimal.ZERO, (left, right) -> left.add(right, MC));
    }

    public List<RetirementIncomeStreamProjectionResponse> projectAtRetirementAge(
            List<RetirementIncomeStreamRequest> streams, int currentAge, int retirementAge) {
        if (streams == null || streams.isEmpty()) {
            return List.of();
        }
        streams.forEach(this::validateAgeRange);
        return streams.stream()
                .map(stream -> {
                    boolean active = isActiveAtAge(stream, retirementAge);
                    BigDecimal projected = active
                            ? projectMonthlyAmount(stream, currentAge, retirementAge)
                            : BigDecimal.ZERO;
                    return new RetirementIncomeStreamProjectionResponse(
                            stream.type(), stream.name(), stream.frequency(), stream.startsAtAge(),
                            stream.endsAtAge(), money(projected), active);
                })
                .toList();
    }

    public List<RetirementIncomeStreamProjectionResponse> projectStreamsAtRetirement(
            List<RetirementIncomeStreamRequest> streams, int currentAge, int retirementAge) {
        return projectAtRetirementAge(streams, currentAge, retirementAge);
    }

    public BigDecimal totalMonthlyIncomeAtRetirement(
            List<RetirementIncomeStreamRequest> streams, int currentAge, int retirementAge) {
        return calculateMonthlyIncomeAtAge(streams == null ? List.of() : streams,
                currentAge, retirementAge).setScale(2, RoundingMode.HALF_UP);
    }

    /** Empty-list convenience method; non-empty streams require age assumptions. */
    public BigDecimal totalMonthlyIncomeAtRetirement(List<RetirementIncomeStreamRequest> streams) {
        if (streams == null || streams.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        throw new IllegalArgumentException(
                "Current age and retirement age are required to project income streams");
    }

    private void validateAgeRange(RetirementIncomeStreamRequest stream) {
        if (stream.endsAtAge() != null && stream.endsAtAge() < stream.startsAtAge()) {
            throw new IllegalArgumentException(
                    "Income end age must be greater than or equal to start age: " + stream.name());
        }
    }

    private boolean isActiveAtAge(RetirementIncomeStreamRequest stream, int age) {
        return age >= stream.startsAtAge()
                && (stream.endsAtAge() == null || age <= stream.endsAtAge());
    }

    private BigDecimal projectMonthlyAmount(
            RetirementIncomeStreamRequest stream, int currentAge, int targetAge) {
        BigDecimal monthlyAmount = toMonthlyAmount(stream.amount(), stream.frequency());
        int years = Math.max(0, targetAge - currentAge);
        BigDecimal annualGrowth = stream.annualGrowthPercentage().divide(HUNDRED, MC);
        BigDecimal growthFactor = ONE.add(annualGrowth, MC).pow(years, MC);
        return monthlyAmount.multiply(growthFactor, MC);
    }

    private BigDecimal toMonthlyAmount(BigDecimal amount, RetirementIncomeFrequency frequency) {
        return switch (frequency) {
            case MONTHLY -> amount;
            case QUARTERLY -> amount.divide(THREE, MC);
            case ANNUALLY, YEARLY -> amount.divide(TWELVE, MC);
        };
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
