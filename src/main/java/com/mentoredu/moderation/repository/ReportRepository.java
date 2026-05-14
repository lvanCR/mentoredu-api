package com.mentoredu.moderation.repository;

import com.mentoredu.moderation.model.Report;
import com.mentoredu.moderation.model.enums.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    boolean existsByReportedByIdAndTargetTypeAndTargetId(UUID reportedById, TargetType targetType, UUID targetId);
}
