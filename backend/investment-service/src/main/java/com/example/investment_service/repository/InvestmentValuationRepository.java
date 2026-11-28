package com.example.investment_service.repository;

import com.example.investment_service.model.valuation.InvestmentValuation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentValuationRepository extends JpaRepository<InvestmentValuation, UUID> {

    List<InvestmentValuation> findAllByInvestment_IdAndIdentityUserIdOrderByValuationDateDescCreatedAtDesc(
            UUID investmentId, Long identityUserId);

    Optional<InvestmentValuation> findFirstByInvestment_IdAndIdentityUserIdOrderByValuationDateDescCreatedAtDesc(
            UUID investmentId, Long identityUserId);

    List<InvestmentValuation> findAllByIdentityUserIdOrderByValuationDateDescCreatedAtDesc(
            Long identityUserId);
}
