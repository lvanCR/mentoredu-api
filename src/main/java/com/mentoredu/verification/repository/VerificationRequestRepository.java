package com.mentoredu.verification.repository;

import com.mentoredu.verification.model.VerificationRequest;
import com.mentoredu.verification.model.enums.EntityType;
import com.mentoredu.verification.model.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, UUID> {

    List<VerificationRequest> findByUserIdOrderBySubmittedAtDesc(UUID userId);

    boolean existsByUserIdAndEntityTypeAndStatus(UUID userId, EntityType entityType, VerificationStatus status);
}
