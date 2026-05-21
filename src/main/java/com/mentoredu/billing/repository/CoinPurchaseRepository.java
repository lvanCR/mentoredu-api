package com.mentoredu.billing.repository;

import com.mentoredu.billing.model.CoinPurchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CoinPurchaseRepository extends JpaRepository<CoinPurchase, UUID> {
    List<CoinPurchase> findByUserId(UUID userId);
}
