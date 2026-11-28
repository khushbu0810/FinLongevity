package com.example.financial_engine_service.engine;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Contains the accumulation-phase financial operations used by retirement projections.
 *
 * <p>Contribution timing is explicitly end-of-month: each month's existing corpus earns
 * the monthly return first, then that month's contribution is added.</p>
 */
public final class RetirementAccumulationCalculator {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final BigDecimal ELEVEN = BigDecimal.valueOf(11);
    private static final int NEWTON_ITERATIONS = 20;

    /**
     * Converts an effective annual return percentage into an effective monthly rate.
     * The resulting monthly rate compounds to the supplied annual rate over 12 months.
     */
    public BigDecimal effectiveMonthlyRate(BigDecimal annualReturnPercentage) {
        Objects.requireNonNull(annualReturnPercentage, "annualReturnPercentage must not be null");

        BigDecimal annualRate = annualReturnPercentage.divide(HUNDRED, MC);
        BigDecimal annualGrowthFactor = ONE.add(annualRate, MC);

        if (annualRate.signum() == 0) {
            return BigDecimal.ZERO;
        }

        // Use double precision only for a starting estimate; Newton iterations refine
        // the twelfth root using BigDecimal arithmetic and the configured MathContext.
        BigDecimal monthlyGrowthFactor = BigDecimal.valueOf(
                Math.pow(annualGrowthFactor.doubleValue(), 1.0d / 12.0d));

        for (int i = 0; i < NEWTON_ITERATIONS; i++) {
            BigDecimal denominator = monthlyGrowthFactor.pow(11, MC);
            BigDecimal quotient = annualGrowthFactor.divide(denominator, MC);
            BigDecimal next = monthlyGrowthFactor.multiply(ELEVEN, MC)
                    .add(quotient, MC)
                    .divide(TWELVE, MC);

            if (next.compareTo(monthlyGrowthFactor) == 0) {
                monthlyGrowthFactor = next;
                break;
            }
            monthlyGrowthFactor = next;
        }

        return monthlyGrowthFactor.subtract(ONE, MC);
    }

    /**
     * Applies one month of growth and then adds the end-of-month contribution.
     */
    public BigDecimal applyEndOfMonthContribution(BigDecimal corpus,
                                                   BigDecimal monthlyContribution,
                                                   BigDecimal monthlyRate) {
        Objects.requireNonNull(corpus, "corpus must not be null");
        Objects.requireNonNull(monthlyContribution, "monthlyContribution must not be null");
        Objects.requireNonNull(monthlyRate, "monthlyRate must not be null");

        return corpus.multiply(ONE.add(monthlyRate, MC), MC)
                .add(monthlyContribution, MC);
    }

    /** Applies annual contribution escalation for the next projection year. */
    public BigDecimal growContributionForNextYear(BigDecimal monthlyContribution,
                                                   BigDecimal annualContributionGrowthRate) {
        Objects.requireNonNull(monthlyContribution, "monthlyContribution must not be null");
        Objects.requireNonNull(annualContributionGrowthRate,
                "annualContributionGrowthRate must not be null");

        return monthlyContribution.multiply(
                ONE.add(annualContributionGrowthRate, MC), MC);
    }
}
