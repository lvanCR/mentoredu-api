package com.mentoredu.community.repository;

import com.mentoredu.community.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {
    Page<Report> findByStatus(String status, Pageable pageable);
    boolean existsByReporterIdAndTargetTypeAndTargetId(UUID reporterId, String targetType, UUID targetId);
}
