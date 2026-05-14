package com.mentoredu.subscription.repository;

import com.mentoredu.subscription.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
