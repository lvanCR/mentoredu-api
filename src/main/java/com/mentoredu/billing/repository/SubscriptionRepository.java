package com.mentoredu.billing.repository;

import com.mentoredu.billing.model.Subscription;
import com.mentoredu.billing.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByUserId(UUID userId);
    Optional<Subscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);
}
