package com.mentoredu.community.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.community.dto.ReportRequest;
import com.mentoredu.community.dto.ReportResponse;
import com.mentoredu.community.dto.ResolveReportRequest;
import com.mentoredu.community.exception.DuplicateReportException;
import com.mentoredu.community.exception.ReportAlreadyResolvedException;
import com.mentoredu.community.exception.ReportNotFoundException;
import com.mentoredu.community.model.Report;
import com.mentoredu.community.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService implements IReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReportResponse create(ReportRequest request, String reporterEmail) {
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + reporterEmail));

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
    public List<ReportResponse> listOpen() {
        return reportRepository.findByStatus("OPEN")
                .stream()
                .map(ReportResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public ReportResponse resolve(UUID reportId, ResolveReportRequest request, String resolverEmail) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Reporte no encontrado: " + reportId));

        if ("RESOLVED".equals(report.getStatus())) {
            throw new ReportAlreadyResolvedException("El reporte ya fue resuelto");
        }

        User resolver = userRepository.findByEmail(resolverEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + resolverEmail));

        report.setStatus("RESOLVED");
        report.setResolvedBy(resolver);
        report.setResolutionNote(request.resolutionNote());
        report.setResolvedAt(LocalDateTime.now());

        // Log simple de auditoría (RN-19)
        log.info("Report {} resolved by {} as {}. Note: {}", 
                reportId, resolverEmail, report.getStatus(), request.resolutionNote());

        return ReportResponse.from(reportRepository.save(report));
    }
}
