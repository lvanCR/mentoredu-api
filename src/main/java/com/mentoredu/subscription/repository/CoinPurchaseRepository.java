package com.mentoredu.subscription.repository;

import com.mentoredu.subscription.model.CoinPurchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CoinPurchaseRepository extends JpaRepository<CoinPurchase, UUID> {
    List<CoinPurchase> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
