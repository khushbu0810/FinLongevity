package com.example.customer_service.repository;

import com.example.customer_service.model.asset_liabilities.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID> {

    List<Asset> findAllByIdentityUserIdOrderByCreatedAtDesc(String identityUserId);

    List<Asset> findAllByIdentityUserIdAndCurrency(String identityUserId, String currency);

    Optional<Asset> findByIdAndIdentityUserId(UUID id, String identityUserId);

}
