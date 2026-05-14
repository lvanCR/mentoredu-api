package com.mentoredu.subscription.repository;

import com.mentoredu.subscription.model.Subscription;
import com.mentoredu.subscription.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);
    List<Subscription> findByUserId(UUID userId);
}
