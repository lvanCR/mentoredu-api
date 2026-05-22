package com.mentoredu.community.repository;

import com.mentoredu.community.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {
    List<Report> findByStatus(String status);
    boolean existsByReporterIdAndTargetTypeAndTargetId(UUID reporterId, String targetType, UUID targetId);
}
