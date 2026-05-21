package com.mentoredu.moderation.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.forum.model.Answer;
import com.mentoredu.forum.model.Comment;
import com.mentoredu.forum.repository.AnswerRepository;
import com.mentoredu.forum.repository.CommentRepository;
import com.mentoredu.forum.repository.ThreadRepository;
import com.mentoredu.library.model.AcademicResource;
import com.mentoredu.library.model.ResourceSolution;
import com.mentoredu.library.repository.AcademicResourceRepository;
import com.mentoredu.library.repository.ResourceSolutionRepository;
import com.mentoredu.moderation.dto.ReportRequest;
import com.mentoredu.moderation.dto.ReportResponse;
import com.mentoredu.moderation.dto.ResolveReportRequest;
import com.mentoredu.moderation.dto.ResolveReportResponse;
import com.mentoredu.moderation.exception.DuplicateReportException;
import com.mentoredu.moderation.exception.ReportAlreadyResolvedException;
import com.mentoredu.moderation.exception.ReportNotFoundException;
import com.mentoredu.moderation.exception.ReportedContentNotFoundException;
import com.mentoredu.moderation.exception.SelfReportException;
import com.mentoredu.moderation.exception.UnauthorizedModerationException;
import com.mentoredu.moderation.model.AuditLog;
import com.mentoredu.moderation.model.ModerationAction;
import com.mentoredu.moderation.model.Report;
import com.mentoredu.moderation.model.enums.ReportStatus;
import com.mentoredu.moderation.model.enums.TargetType;
import com.mentoredu.moderation.repository.AuditLogRepository;
import com.mentoredu.moderation.repository.ModerationActionRepository;
import com.mentoredu.moderation.repository.ReportRepository;
import com.mentoredu.forum.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService implements IReportService {

    private final ReportRepository reportRepository;
    private final AuditLogRepository auditLogRepository;
    private final ModerationActionRepository moderationActionRepository;
    private final UserRepository userRepository;
    private final ThreadRepository threadRepository;
    private final AnswerRepository answerRepository;
    private final CommentRepository commentRepository;
    private final AcademicResourceRepository academicResourceRepository;
    private final ResourceSolutionRepository resourceSolutionRepository;

    @Override
    @Transactional
    public ReportResponse create(ReportRequest request, String reporterEmail) {
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + reporterEmail));

        UUID targetAuthorId = resolveTargetAuthorId(request.getTargetType(), request.getTargetId());

        if (targetAuthorId.equals(reporter.getId())) {
            throw new SelfReportException("No puedes reportar tu propio contenido");
        }

        if (reportRepository.existsByReportedByIdAndTargetTypeAndTargetId(
                reporter.getId(), request.getTargetType(), request.getTargetId())) {
            throw new DuplicateReportException("Ya has reportado este contenido anteriormente");
        }

        Report report = Report.builder()
                .reportedBy(reporter)
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .reason(request.getReason())
                .build();

        Report saved = reportRepository.save(report);

        // RN-22: toda acción de moderación registra entrada en audit_logs
        AuditLog auditLog = AuditLog.builder()
                .actor(reporter)
                .actionType("REPORT_CREATED")
                .entityType("REPORT")
                .entityId(saved.getId())
                .detail("Reported " + request.getTargetType().name() + " " + request.getTargetId())
                .build();
        auditLogRepository.save(auditLog);

        return new ReportResponse(saved);
    }

    @Override
    @Transactional
    public ResolveReportResponse resolve(UUID reportId, ResolveReportRequest request, String moderatorEmail) {
        User moderator = userRepository.findByEmail(moderatorEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + moderatorEmail));

        // RN-21: solo moderadores o admins pueden resolver reportes
        String roleName = moderator.getRole().getName();
        if (!"MODERATOR".equals(roleName) && !"ADMIN".equals(roleName)) {
            throw new UnauthorizedModerationException("No tienes permisos para resolver reportes");
        }

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Reporte no encontrado: " + reportId));

        // Alternativo error: reporte ya fue resuelto — rechaza el cambio por estado inválido
        if (report.getStatus() == ReportStatus.RESOLVED || report.getStatus() == ReportStatus.REJECTED) {
            throw new ReportAlreadyResolvedException(
                    "El reporte ya fue resuelto con estado: " + report.getStatus().name());
        }

        // Valida que la resolución sea un estado terminal
        if (request.getResolution() == ReportStatus.OPEN || request.getResolution() == ReportStatus.IN_REVIEW) {
            throw new IllegalArgumentException("La resolución debe ser RESOLVED o REJECTED");
        }

        report.setStatus(request.getResolution());
        report.setResolvedBy(moderator.getId());
        report.setResolvedAt(LocalDateTime.now());
        reportRepository.save(report);

        ModerationAction action = ModerationAction.builder()
                .report(report)
                .moderator(moderator)
                .actionType(request.getActionType().name())
                .notes(request.getNotes())
                .build();
        moderationActionRepository.save(action);

        // RN-22: toda acción de moderación registra entrada en audit_logs
        AuditLog auditLog = AuditLog.builder()
                .actor(moderator)
                .actionType("REPORT_" + request.getResolution().name())
                .entityType("REPORT")
                .entityId(reportId)
                .detail("Resolved report " + reportId + " as " + request.getResolution().name()
                        + " with action " + request.getActionType().name())
                .build();
        auditLogRepository.save(auditLog);

        return new ResolveReportResponse(report, action);
    }

    private UUID resolveTargetAuthorId(TargetType targetType, UUID targetId) {
        return switch (targetType) {
            case THREAD -> {
                com.mentoredu.forum.model.Thread thread = threadRepository.findById(targetId)
                        .orElseThrow(() -> new ReportedContentNotFoundException(
                                "Hilo no encontrado: " + targetId));
                yield thread.getAuthor().getId();
            }
            case ANSWER -> {
                Answer answer = answerRepository.findById(targetId)
                        .orElseThrow(() -> new ReportedContentNotFoundException(
                                "Respuesta no encontrada: " + targetId));
                yield answer.getAuthor().getId();
            }
            case COMMENT -> {
                Comment comment = commentRepository.findById(targetId)
                        .orElseThrow(() -> new ReportedContentNotFoundException(
                                "Comentario no encontrado: " + targetId));
                yield comment.getAuthor().getId();
            }
            case RESOURCE -> {
                AcademicResource resource = academicResourceRepository.findById(targetId)
                        .orElseThrow(() -> new ReportedContentNotFoundException(
                                "Recurso no encontrado: " + targetId));
                yield resource.getAuthor().getId();
            }
            case SOLUTION -> {
                ResourceSolution solution = resourceSolutionRepository.findById(targetId)
                        .orElseThrow(() -> new ReportedContentNotFoundException(
                                "Solución no encontrada: " + targetId));
                yield solution.getStudentId();
            }
        };
    }
}
