package com.mentoredu.community.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.service.UserService;
import com.mentoredu.config.PagedResponse;
import com.mentoredu.community.dto.ReportRequest;
import com.mentoredu.community.dto.ReportResponse;
import com.mentoredu.community.dto.ResolveReportRequest;
import com.mentoredu.community.exception.DuplicateReportException;
import com.mentoredu.community.exception.ReportAlreadyResolvedException;
import com.mentoredu.community.exception.ReportNotFoundException;
import com.mentoredu.community.model.ModerationAuditLog;
import com.mentoredu.community.model.Report;
import com.mentoredu.community.repository.ModerationAuditLogRepository;
import com.mentoredu.community.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService implements IReportService {

    private final ReportRepository           reportRepository;
    private final UserService                userService;
    private final ModerationAuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public ReportResponse create(ReportRequest request, String reporterEmail) {
        User reporter = userService.findByEmailOrThrow(reporterEmail);

        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                reporter.getId(), request.targetType(), request.targetId())) {
            throw new DuplicateReportException("Ya reportaste este contenido");
        }

        Report report = Report.builder()
                .reporter(reporter)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .reason(request.reason())
                .status("OPEN")
                .build();

        return ReportResponse.from(reportRepository.save(report));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReportResponse> listOpen(int page, int size) {
        return PagedResponse.from(
                reportRepository.findByStatus("OPEN", PagedResponse.toPageRequest(page, size)),
                ReportResponse::from);
    }

    @Override
    @Transactional
    public ReportResponse resolve(UUID reportId, ResolveReportRequest request, String resolverEmail) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Reporte no encontrado: " + reportId));

        if ("RESOLVED".equals(report.getStatus())) {
            throw new ReportAlreadyResolvedException("El reporte ya fue resuelto");
        }

        User resolver = userService.findByEmailOrThrow(resolverEmail);

        report.setStatus("RESOLVED");
        report.setResolvedBy(resolver);
        report.setResolutionNote(request.resolutionNote());
        report.setResolvedAt(LocalDateTime.now());

        Report saved = reportRepository.save(report);

        auditLogRepository.save(ModerationAuditLog.builder()
                .report(saved)
                .actor(resolver)
                .action("RESOLVED")
                .note(request.resolutionNote())
                .build());

        return ReportResponse.from(saved);
    }
}