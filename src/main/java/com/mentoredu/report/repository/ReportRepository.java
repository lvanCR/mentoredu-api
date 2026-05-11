package com.mentoredu.report.repository;

import com.mentoredu.report.model.Report;
import com.mentoredu.report.model.enums.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReportedByIdAndTargetTypeAndTargetId(Long reportedById, TargetType targetType, Long targetId);
}
