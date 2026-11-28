package com.example.investment_service.service;

import com.example.investment_service.dto.valuation.InvestmentValuationOverviewItemResponse;
import com.example.investment_service.dto.valuation.InvestmentValuationOverviewResponse;
import com.example.investment_service.dto.valuation.InvestmentValuationRequest;
import com.example.investment_service.dto.valuation.InvestmentValuationResponse;
import com.example.investment_service.model.investment.Investment;
import com.example.investment_service.model.investment.InvestmentStatus;
import com.example.investment_service.model.transaction.InvestmentTransaction;
import com.example.investment_service.model.transaction.InvestmentTransactionStatus;
import com.example.investment_service.model.transaction.InvestmentTransactionType;
import com.example.investment_service.model.valuation.InvestmentValuation;
import com.example.investment_service.model.valuation.InvestmentValuationStatus;
import com.example.investment_service.repository.InvestmentRepository;
import com.example.investment_service.repository.InvestmentTransactionRepository;
import com.example.investment_service.repository.InvestmentValuationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class InvestmentValuationService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final InvestmentRepository investmentRepository;
    private final InvestmentTransactionRepository transactionRepository;
    private final InvestmentValuationRepository valuationRepository;

    public InvestmentValuationService(
            InvestmentRepository investmentRepository,
            InvestmentTransactionRepository transactionRepository,
            InvestmentValuationRepository valuationRepository) {
        this.investmentRepository = investmentRepository;
        this.transactionRepository = transactionRepository;
        this.valuationRepository = valuationRepository;
    }

    public InvestmentValuationResponse recordValuation(
            Long userId, UUID investmentId, InvestmentValuationRequest request) {
        Investment investment = findOwnedInvestment(userId, investmentId);
        ensureValuationEligible(investment);

        if (request.valuationDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Valuation date cannot be in the future");
        }
        if (request.marketValue().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Market value cannot be negative");
        }

        InvestmentValuation valuation = new InvestmentValuation();
        valuation.setInvestment(investment);
        valuation.setIdentityUserId(userId);
        valuation.setProductNameSnapshot(investment.getProductNameSnapshot());
        valuation.setMarketValue(request.marketValue());
        valuation.setValuationDate(request.valuationDate());
        valuation.setValuationSource(request.valuationSource());
        valuation.setSourceReference(normalizeSourceReference(request.sourceReference()));

        InvestmentValuation saved = valuationRepository.save(valuation);
        return toResponse(saved, investment);
    }

    @Transactional(readOnly = true)
    public List<InvestmentValuationResponse> getValuationHistory(Long userId, UUID investmentId) {
        Investment investment = findOwnedInvestment(userId, investmentId);
        return valuationRepository
                .findAllByInvestment_IdAndIdentityUserIdOrderByValuationDateDescCreatedAtDesc(
                        investmentId, userId)
                .stream()
                .map(valuation -> toResponse(valuation, investment))
                .toList();
    }

    @Transactional(readOnly = true)
    public InvestmentValuationOverviewResponse getValuationOverview(Long userId) {
        List<Investment> eligibleInvestments = investmentRepository
                .findAllByIdentityUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(this::isValuationEligible)
                .toList();

        // The repository ordering places the newest valuation first. Keep the first
        // record for each investment so the overview does not issue one query per row.
        Map<UUID, InvestmentValuation> latestByInvestmentId = new HashMap<>();
        for (InvestmentValuation valuation : valuationRepository
                .findAllByIdentityUserIdOrderByValuationDateDescCreatedAtDesc(userId)) {
            latestByInvestmentId.putIfAbsent(valuation.getInvestment().getId(), valuation);
        }

        List<InvestmentValuationOverviewItemResponse> items = new ArrayList<>();
        for (Investment investment : eligibleInvestments) {
            InvestmentValuation latest = latestByInvestmentId.get(investment.getId());
            items.add(new InvestmentValuationOverviewItemResponse(
                    investment.getId(),
                    investment.getProductNameSnapshot(),
                    investment.getStatus(),
                    investment.getCurrency(),
                    calculateNetInvestedPrincipal(investment, userId),
                    latest == null ? null : toResponse(latest, investment),
                    latest == null
                            ? InvestmentValuationStatus.UNAVAILABLE
                            : InvestmentValuationStatus.AVAILABLE
            ));
        }
        return new InvestmentValuationOverviewResponse(items);
    }

    private BigDecimal calculateNetInvestedPrincipal(Investment investment, Long userId) {
        BigDecimal netPrincipal = ZERO;
        String investmentCurrency = normalizeCurrency(investment.getCurrency());

        List<InvestmentTransaction> transactions = transactionRepository
                .findAllByInvestmentIdAndIdentityUserIdOrderByCreatedAtDesc(
                        investment.getId(), userId);

        for (InvestmentTransaction transaction : transactions) {
            if (transaction.getStatus() != InvestmentTransactionStatus.SUCCESS
                    || transaction.getAmount() == null
                    || transaction.getAmount().signum() <= 0
                    || investmentCurrency == null
                    || !investmentCurrency.equals(normalizeCurrency(transaction.getCurrency()))) {
                continue;
            }

            InvestmentTransactionType type = transaction.getTransactionType();
            if (type == InvestmentTransactionType.PURCHASE
                    || type == InvestmentTransactionType.ADDITIONAL_INVESTMENT) {
                netPrincipal = netPrincipal.add(transaction.getAmount());
            } else if (type == InvestmentTransactionType.PARTIAL_REDEMPTION
                    || type == InvestmentTransactionType.FULL_REDEMPTION) {
                netPrincipal = netPrincipal.subtract(transaction.getAmount());
            }
        }

        // Keep the same conservative principal-evidence rule used by the Financial Engine:
        // successful redemptions cannot produce negative supported principal.
        return netPrincipal.max(ZERO);
    }

    private Investment findOwnedInvestment(Long userId, UUID investmentId) {
        return investmentRepository.findByIdAndIdentityUserId(investmentId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Investment not found"));
    }

    private void ensureValuationEligible(Investment investment) {
        if (!isValuationEligible(investment)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Valuations can only be recorded for ACTIVE or PARTIALLY_REDEEMED investments");
        }
    }

    private boolean isValuationEligible(Investment investment) {
        return investment.getStatus() == InvestmentStatus.ACTIVE
                || investment.getStatus() == InvestmentStatus.PARTIALLY_REDEEMED;
    }

    private InvestmentValuationResponse toResponse(
            InvestmentValuation valuation, Investment investment) {
        return new InvestmentValuationResponse(
                valuation.getId(),
                investment.getId(),
                valuation.getProductNameSnapshot(),
                valuation.getMarketValue(),
                investment.getCurrency(),
                valuation.getValuationDate(),
                valuation.getValuationSource(),
                valuation.getSourceReference(),
                valuation.getCreatedAt()
        );
    }

    private String normalizeCurrency(String currency) {
        return currency == null ? null : currency.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeSourceReference(String sourceReference) {
        return sourceReference == null || sourceReference.isBlank()
                ? null : sourceReference.trim();
    }
}
