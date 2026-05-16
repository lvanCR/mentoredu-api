package com.mentoredu.verification.repository;

import com.mentoredu.verification.model.VerificationDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VerificationDocumentRepository extends JpaRepository<VerificationDocument, UUID> {
    List<VerificationDocument> findByRequestId(UUID requestId);
}
