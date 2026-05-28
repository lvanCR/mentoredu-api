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
import com.mentoredu.community.exception.ReportTargetNotFoundException;
import com.mentoredu.community.model.ModerationAuditLog;
import com.mentoredu.community.model.ModerationAction;
import com.mentoredu.community.model.Report;
import com.mentoredu.community.model.ReportStatus;
import com.mentoredu.community.repository.ModerationAuditLogRepository;
import com.mentoredu.community.repository.ReportRepository;
import com.mentoredu.forum.repository.AnswerRepository;
import com.mentoredu.forum.repository.CommentRepository;
import com.mentoredu.forum.repository.ThreadRepository;
import com.mentoredu.library.repository.ResourceRepository;
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

    private final ReportRepository             reportRepository;
    private final UserService                  userService;
    private final ModerationAuditLogRepository auditLogRepository;
    private final ThreadRepository             threadRepository;
    private final AnswerRepository             answerRepository;
    private final CommentRepository            commentRepository;
    private final ResourceRepository           resourceRepository;

    @Override
    @Transactional
    public ReportResponse create(ReportRequest request, String reporterEmail) {
        User reporter = userService.findByEmailOrThrow(reporterEmail);

        validateTargetExists(request.targetType(), request.targetId());

        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                reporter.getId(), request.targetType(), request.targetId())) {
            throw new DuplicateReportException("Ya reportaste este contenido");
        }

        Report report = Report.builder()
                .reporter(reporter)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .reason(request.reason())
                .status(ReportStatus.OPEN)
                .build();

        return ReportResponse.from(reportRepository.save(report));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReportResponse> listOpen(int page, int size) {
        return PagedResponse.from(
                reportRepository.findByStatus(ReportStatus.OPEN, PagedResponse.toPageRequest(page, size)),
                ReportResponse::from);
    }

    @Override
    @Transactional
    public ReportResponse resolve(UUID reportId, ResolveReportRequest request, String resolverEmail) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Reporte no encontrado: " + reportId));

        if (ReportStatus.RESOLVED == report.getStatus()) {
            throw new ReportAlreadyResolvedException("El reporte ya fue resuelto");
        }

        User resolver = userService.findByEmailOrThrow(resolverEmail);

        report.setStatus(ReportStatus.RESOLVED);
        report.setResolvedBy(resolver);
        report.setResolutionNote(request.resolutionNote());
        report.setResolvedAt(LocalDateTime.now());

        Report saved = reportRepository.save(report);

        auditLogRepository.save(ModerationAuditLog.builder()
                .report(saved)
                .actor(resolver)
                .action(ModerationAction.RESOLVED)
                .note(request.resolutionNote())
                .build());

        return ReportResponse.from(saved);
    }

    private void validateTargetExists(String targetType, UUID targetId) {
        boolean exists = switch (targetType) {
            case "THREAD"   -> threadRepository.existsById(targetId);
            case "ANSWER"   -> answerRepository.existsById(targetId);
            case "COMMENT"  -> commentRepository.existsById(targetId);
            case "RESOURCE" -> resourceRepository.existsById(targetId);
            default -> true;
        };
        if (!exists) {
            throw new ReportTargetNotFoundException(
                "El contenido reportado no existe: " + targetType + " " + targetId);
        }
    }
}