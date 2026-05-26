package com.mentoredu.community.repository;

import com.mentoredu.community.model.ModerationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ModerationAuditLogRepository extends JpaRepository<ModerationAuditLog, UUID> {
}
