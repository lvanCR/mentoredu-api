package com.mentoredu.report.repository;

import com.mentoredu.report.model.ModerationAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ModerationActionRepository extends JpaRepository<ModerationAction, UUID> {
    List<ModerationAction> findByReportId(UUID reportId);
}
