package com.mentoredu.billing.repository;

import com.mentoredu.billing.model.PremiumAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PremiumAccessRepository extends JpaRepository<PremiumAccess, UUID> {
    List<PremiumAccess> findByUserId(UUID userId);
}
