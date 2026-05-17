package com.mentoredu.moderation.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.forum.model.Answer;
import com.mentoredu.forum.model.Comment;
import com.mentoredu.forum.repository.AnswerRepository;
import com.mentoredu.forum.repository.CommentRepository;
import com.mentoredu.forum.repository.ThreadRepository;
import com.mentoredu.library.model.AcademicResource;
import com.mentoredu.library.repository.AcademicResourceRepository;
import com.mentoredu.moderation.dto.ReportRequest;
import com.mentoredu.moderation.dto.ReportResponse;
import com.mentoredu.moderation.exception.DuplicateReportException;
import com.mentoredu.moderation.exception.ReportedContentNotFoundException;
import com.mentoredu.moderation.exception.SelfReportException;
import com.mentoredu.moderation.model.AuditLog;
import com.mentoredu.moderation.model.Report;
import com.mentoredu.moderation.model.enums.TargetType;
import com.mentoredu.moderation.repository.AuditLogRepository;
import com.mentoredu.moderation.repository.ReportRepository;
import com.mentoredu.forum.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService implements IReportService {

    private final ReportRepository reportRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ThreadRepository threadRepository;
    private final AnswerRepository answerRepository;
    private final CommentRepository commentRepository;
    private final AcademicResourceRepository academicResourceRepository;

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
        };
    }
}
