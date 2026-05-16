package com.mentoredu.moderation.repository;

import com.mentoredu.moderation.model.ModerationAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ModerationActionRepository extends JpaRepository<ModerationAction, UUID> {
    List<ModerationAction> findByReportId(UUID reportId);
}
