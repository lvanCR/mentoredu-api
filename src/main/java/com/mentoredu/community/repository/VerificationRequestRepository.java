package com.mentoredu.community.repository;

import com.mentoredu.community.model.VerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, UUID> {
    List<VerificationRequest> findByUserId(UUID userId);
    boolean existsByUserIdAndStatus(UUID userId, String status);
}
