package com.mentoredu.verification.repository;

import com.mentoredu.verification.model.VerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, UUID> {
    List<VerificationRequest> findByUserId(UUID userId);
}
