package com.example.customer_service.service;

import com.example.customer_service.config.GatewayUserPrincipal;
import com.example.customer_service.dto.asset_liabilities.*;
import com.example.customer_service.model.asset_liabilities.Asset;
import com.example.customer_service.model.asset_liabilities.Liability;
import com.example.customer_service.repository.AssetRepository;
import com.example.customer_service.repository.LiabilityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class AssetLiabilityService {
    private final AssetRepository assetRepository;
    private final LiabilityRepository liabilityRepository;

    public AssetLiabilityService(AssetRepository assetRepository, LiabilityRepository liabilityRepository) {
        this.assetRepository = assetRepository;
        this.liabilityRepository = liabilityRepository;
    }

    public AssetResponse createAsset(AssetRequest r) {
        Asset a = new Asset();
        a.setIdentityUserId(currentUserId());
        applyAsset(a, r);
        return assetResponse(assetRepository.save(a));
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getAssets() {
        return assetRepository.findAllByIdentityUserIdOrderByCreatedAtDesc(currentUserId())
                .stream()
                .map(this::assetResponse)
                .toList();
    }

    public AssetResponse updateAsset(UUID id, AssetRequest r) {
        Asset a = assetRepository.findByIdAndIdentityUserId(id, currentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));
        applyAsset(a, r);
        return assetResponse(assetRepository.save(a));
    }

    public void deleteAsset(UUID id) {
        Asset a = assetRepository.findByIdAndIdentityUserId(id, currentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));
        assetRepository.delete(a);
    }

    public LiabilityResponse createLiability(LiabilityRequest r) {
        Liability l = new Liability();
        l.setIdentityUserId(currentUserId());
        applyLiability(l, r);
        return liabilityResponse(liabilityRepository.save(l));
    }

    @Transactional(readOnly = true)
    public List<LiabilityResponse> getLiabilities() {
        return liabilityRepository.findAllByIdentityUserIdOrderByCreatedAtDesc(currentUserId())
                .stream()
                .map(this::liabilityResponse)
                .toList();
    }

    public LiabilityResponse updateLiability(UUID id, LiabilityRequest r) {
        Liability l = liabilityRepository.findByIdAndIdentityUserId(id, currentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liability not found"));
        applyLiability(l, r);
        return liabilityResponse(liabilityRepository.save(l));
    }

    public void deleteLiability(UUID id) {
        Liability l = liabilityRepository.findByIdAndIdentityUserId(id, currentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liability not found"));
        liabilityRepository.delete(l);
    }

    @Transactional(readOnly = true)
    public NetWorthResponse calculateNetWorth(String inputCurrency) {
        String userId = currentUserId();
        String currency = normalizeCurrency(inputCurrency);
        BigDecimal assets = assetRepository.findAllByIdentityUserIdAndCurrency(userId, currency)
                .stream()
                .map(Asset::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal liabilities = liabilityRepository.findAllByIdentityUserIdAndCurrency(userId, currency)
                .stream()
                .map(Liability::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new NetWorthResponse(
                currency,
                assets,
                liabilities,
                assets.subtract(liabilities),
                Instant.now()
        );
    }

    private void applyAsset(Asset a, AssetRequest r) {
        a.setName(r.name());
        a.setCategory(r.category());
        a.setCurrentValue(r.currentValue());
        a.setCurrency(normalizeCurrency(r.currency()));
        a.setValuationDate(r.valuationDate());
        a.setLiquid(r.liquid());
        a.setNotes(r.notes());
    }

    private void applyLiability(Liability l, LiabilityRequest r) {
        l.setName(r.name());
        l.setCategory(r.category());
        l.setOutstandingAmount(r.outstandingAmount());
        l.setCurrency(normalizeCurrency(r.currency()));
        l.setAnnualInterestRate(r.annualInterestRate());
        l.setMonthlyEmi(r.monthlyEmi());
        l.setNextPaymentDate(r.nextPaymentDate());
        l.setNotes(r.notes());
    }

    private AssetResponse assetResponse(Asset a) {
        return new AssetResponse(a.getId(), a.getName(), a.getCategory(), a.getCurrentValue(), a.getCurrency(),
                a.getValuationDate(), a.isLiquid(), a.getNotes(), a.getCreatedAt(), a.getUpdatedAt());
    }

    private LiabilityResponse liabilityResponse(Liability l) {
        return new LiabilityResponse(l.getId(), l.getName(), l.getCategory(), l.getOutstandingAmount(), l.getCurrency(),
                l.getAnnualInterestRate(), l.getMonthlyEmi(), l.getNextPaymentDate(), l.getNotes(), l.getCreatedAt(), l.getUpdatedAt());
    }

    private String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof GatewayUserPrincipal p)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user context is missing");
        }
        return p.userId();
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || !currency.trim().matches("[A-Za-z]{3}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currency must be a 3-letter code, for example INR");
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }
}
