package com.example.customer_service.repository;

import com.example.customer_service.model.nominee.Nominee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NomineeRepository extends JpaRepository<Nominee, UUID> {

    List<Nominee> findAllByIdentityUserIdOrderByCreatedAtDesc(String identityUserId);

    Optional<Nominee> findByIdAndIdentityUserId(UUID id, String identityUserId);

    boolean existsByIdentityUserId(String identityUserId);
}
