package com.example.investment_service.service;

import com.example.investment_service.dto.investment.InvestmentRequest;
import com.example.investment_service.dto.investment.InvestmentResponse;
import com.example.investment_service.dto.transaction.InvestmentTransactionRequest;
import com.example.investment_service.dto.transaction.InvestmentTransactionResponse;
import com.example.investment_service.model.investment.Investment;
import com.example.investment_service.model.investment.InvestmentStatus;
import com.example.investment_service.model.transaction.InvestmentTransaction;
import com.example.investment_service.model.transaction.InvestmentTransactionStatus;
import com.example.investment_service.model.transaction.InvestmentTransactionType;
import com.example.investment_service.repository.InvestmentRepository;
import com.example.investment_service.repository.InvestmentTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final InvestmentTransactionRepository transactionRepository;

    public InvestmentService(InvestmentRepository investmentRepository,
                             InvestmentTransactionRepository transactionRepository) {
        this.investmentRepository = investmentRepository;
        this.transactionRepository = transactionRepository;
    }

    public InvestmentResponse createInvestment(Long userId, InvestmentRequest request) {
        Investment investment = new Investment();
        investment.setIdentityUserId(userId);
        investment.setProviderProductId(request.providerProductId());
        investment.setProductNameSnapshot(request.productNameSnapshot().trim());
        investment.setProductType(request.productType());
        investment.setInvestedAmount(request.investedAmount());
        investment.setCurrency(request.currency().trim().toUpperCase(Locale.ROOT));
        investment.setUnits(request.units());
        investment.setGoalId(request.goalId());
        investment.setStatus(InvestmentStatus.DRAFT);

        Investment saved = investmentRepository.save(investment);

        InvestmentTransaction purchase = new InvestmentTransaction();
        purchase.setInvestment(saved);
        purchase.setIdentityUserId(userId);
        purchase.setTransactionType(InvestmentTransactionType.PURCHASE);
        purchase.setStatus(InvestmentTransactionStatus.INITIATED);
        purchase.setAmount(request.investedAmount());
        purchase.setCurrency(saved.getCurrency());
        purchase.setUnits(request.units());
        purchase.setDescription("Initial purchase transaction created with investment draft");
        transactionRepository.save(purchase);

        return toInvestmentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InvestmentResponse> getMyInvestments(Long userId) {
        return investmentRepository.findAllByIdentityUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toInvestmentResponse).toList();
    }

    @Transactional(readOnly = true)
    public InvestmentResponse getMyInvestment(Long userId, UUID investmentId) {
        return toInvestmentResponse(findOwnedInvestment(userId, investmentId));
    }

    public InvestmentResponse cancelDraft(Long userId, UUID investmentId) {
        Investment investment = findOwnedInvestment(userId, investmentId);
        if (investment.getStatus() != InvestmentStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Only DRAFT investments can be cancelled");
        }
        investment.setStatus(InvestmentStatus.CANCELLED);
        investmentRepository.save(investment);

        List<InvestmentTransaction> transactions =
                transactionRepository.findAllByInvestmentIdAndIdentityUserIdOrderByCreatedAtDesc(investmentId, userId);
        transactions.stream()
                .filter(t -> t.getTransactionType() == InvestmentTransactionType.PURCHASE
                        && t.getStatus() == InvestmentTransactionStatus.INITIATED)
                .forEach(t -> t.setStatus(InvestmentTransactionStatus.FAILED));
        transactionRepository.saveAll(transactions);

        return toInvestmentResponse(investment);
    }

    public InvestmentTransactionResponse addTransaction(
            Long userId, UUID investmentId, InvestmentTransactionRequest request) {
        Investment investment = findOwnedInvestment(userId, investmentId);

        if (investment.getStatus() == InvestmentStatus.CANCELLED
                || investment.getStatus() == InvestmentStatus.REDEEMED
                || investment.getStatus() == InvestmentStatus.FAILED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Transactions are not allowed for this investment status");
        }

        InvestmentTransaction transaction = new InvestmentTransaction();
        transaction.setInvestment(investment);
        transaction.setIdentityUserId(userId);
        transaction.setTransactionType(request.transactionType());
        transaction.setStatus(InvestmentTransactionStatus.INITIATED);
        transaction.setAmount(request.amount());
        transaction.setCurrency(request.currency() == null || request.currency().isBlank()
                ? investment.getCurrency()
                : request.currency().trim().toUpperCase(Locale.ROOT));
        transaction.setUnits(request.units());
        transaction.setDescription(request.description());

        return toTransactionResponse(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public List<InvestmentTransactionResponse> getMyTransactions(Long userId, UUID investmentId) {
        findOwnedInvestment(userId, investmentId);
        return transactionRepository
                .findAllByInvestmentIdAndIdentityUserIdOrderByCreatedAtDesc(investmentId, userId)
                .stream().map(this::toTransactionResponse).toList();
    }

    private Investment findOwnedInvestment(Long userId, UUID investmentId) {
        return investmentRepository.findByIdAndIdentityUserId(investmentId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Investment not found"));
    }

    private InvestmentResponse toInvestmentResponse(Investment i) {
        return new InvestmentResponse(
                i.getId(), i.getIdentityUserId(), i.getProviderProductId(),
                i.getProductNameSnapshot(), i.getProductType(), i.getInvestedAmount(),
                i.getCurrency(), i.getUnits(), i.getStatus(), i.getGoalId(),
                i.getOpenedAt(), i.getCreatedAt(), i.getUpdatedAt()
        );
    }

    private InvestmentTransactionResponse toTransactionResponse(InvestmentTransaction t) {
        return new InvestmentTransactionResponse(
                t.getId(), t.getInvestment().getId(), t.getTransactionType(),
                t.getStatus(), t.getAmount(), t.getCurrency(), t.getUnits(),
                t.getPaymentReference(), t.getDescription(), t.getProcessedAt(),
                t.getCreatedAt(), t.getUpdatedAt()
        );
    }
}
