package com.mentoredu.community.repository;

import com.mentoredu.community.model.VerificationRequest;
import com.mentoredu.community.model.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, UUID> {

    @EntityGraph(attributePaths = "docs")
    Page<VerificationRequest> findByUserIdOrderBySubmittedAtDesc(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = "docs")
    Page<VerificationRequest> findAllByOrderBySubmittedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = "docs")
    Page<VerificationRequest> findByStatusOrderBySubmittedAtDesc(VerificationStatus status, Pageable pageable);

    boolean existsByUserIdAndStatus(UUID userId, VerificationStatus status);
}
