package com.mentoredu.community.repository;

import com.mentoredu.community.model.VerificationDoc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VerificationDocRepository extends JpaRepository<VerificationDoc, UUID> {
}
