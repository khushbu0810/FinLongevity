package com.example.financial_engine_service.engine;

import com.example.financial_engine_service.dto.retirement.ScenarioSimulationRequest;
import com.example.financial_engine_service.dto.retirement.ScenarioSimulationResponse;
import com.example.financial_engine_service.enums.ScenarioType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScenarioSimulationCalculator {

    private static final MathContext MC = new MathContext(16, RoundingMode.HALF_UP);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal ONE = BigDecimal.ONE;

    private final RetirementAccumulationCalculator accumulationCalculator =
            new RetirementAccumulationCalculator();

    public List<ScenarioSimulationResponse> simulate(ScenarioSimulationRequest request) {
        List<ScenarioSimulationResponse> results = new ArrayList<>();
        results.add(calculate(request, ScenarioType.CONSERVATIVE,
                request.annualReturnPercentage().subtract(BigDecimal.valueOf(2)).max(BigDecimal.ZERO),
                request.annualInflationPercentage().add(BigDecimal.ONE)));
        results.add(calculate(request, ScenarioType.EXPECTED,
                request.annualReturnPercentage(), request.annualInflationPercentage()));
        results.add(calculate(request, ScenarioType.OPTIMISTIC,
                request.annualReturnPercentage().add(BigDecimal.valueOf(2)).min(HUNDRED),
                request.annualInflationPercentage().subtract(BigDecimal.ONE).max(BigDecimal.ZERO)));
        return List.copyOf(results);
    }

    private ScenarioSimulationResponse calculate(ScenarioSimulationRequest request,
                                                 ScenarioType type,
                                                 BigDecimal annualReturnPercentage,
                                                 BigDecimal annualInflationPercentage) {
        BigDecimal monthlyRate = accumulationCalculator.effectiveMonthlyRate(
                annualReturnPercentage);
        BigDecimal corpus = request.currentCorpus();

        for (int month = 0; month < request.years() * 12; month++) {
            corpus = accumulationCalculator.applyEndOfMonthContribution(
                    corpus, request.monthlyContribution(), monthlyRate);
        }

        BigDecimal inflationFactor = ONE.add(
                annualInflationPercentage.divide(HUNDRED, MC)).pow(request.years(), MC);
        BigDecimal futureMonthlyExpenses = request.currentMonthlyExpenses()
                .multiply(inflationFactor, MC);
        BigDecimal realCorpus = corpus.divide(inflationFactor, MC);

        return new ScenarioSimulationResponse(
                type,
                annualReturnPercentage.setScale(2, RoundingMode.HALF_UP),
                annualInflationPercentage.setScale(2, RoundingMode.HALF_UP),
                corpus.setScale(2, RoundingMode.HALF_UP),
                futureMonthlyExpenses.setScale(2, RoundingMode.HALF_UP),
                realCorpus.setScale(2, RoundingMode.HALF_UP),
                request.years()
        );
    }
}
