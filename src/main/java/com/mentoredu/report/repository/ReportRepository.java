package com.mentoredu.report.repository;

import com.mentoredu.report.model.Report;
import com.mentoredu.report.model.enums.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, java.util.UUID> {

    boolean existsByReportedByIdAndTargetTypeAndTargetId(java.util.UUID reportedById, TargetType targetType, java.util.UUID targetId);
}
