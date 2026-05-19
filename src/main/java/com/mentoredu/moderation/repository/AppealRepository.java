package com.mentoredu.moderation.repository;

import com.mentoredu.moderation.model.Appeal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppealRepository extends JpaRepository<Appeal, UUID> {
    List<Appeal> findByReportId(UUID reportId);
    boolean existsByReportIdAndUserId(UUID reportId, UUID userId);
}
