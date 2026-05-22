package com.mentoredu.moderation.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.forum.exception.UserNotFoundException;
import com.mentoredu.moderation.dto.AppealRequest;
import com.mentoredu.moderation.dto.AppealResponse;
import com.mentoredu.moderation.event.AppealResolvedEvent;
import com.mentoredu.moderation.exception.AppealAlreadyResolvedException;
import com.mentoredu.moderation.exception.DuplicateAppealException;
import com.mentoredu.moderation.exception.ReportNotFoundException;
import com.mentoredu.moderation.exception.UnauthorizedModerationException;
import com.mentoredu.moderation.model.Appeal;
import com.mentoredu.moderation.model.AuditLog;
import com.mentoredu.moderation.model.Report;
import com.mentoredu.moderation.repository.AppealRepository;
import com.mentoredu.moderation.repository.AuditLogRepository;
import com.mentoredu.moderation.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppealService implements IAppealService {

    private final AppealRepository appealRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public AppealResponse submit(AppealRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

        Report report = reportRepository.findById(request.getReportId())
                .orElseThrow(() -> new ReportNotFoundException("Reporte no encontrado: " + request.getReportId()));

        // RN-43: solo una apelación activa por usuario por reporte
        if (appealRepository.existsByReportIdAndUserId(report.getId(), user.getId())) {
            throw new DuplicateAppealException("Ya presentaste una apelación para este reporte");
        }

        Appeal appeal = Appeal.builder()
                .report(report)
                .user(user)
                .reason(request.getReason())
                .build();

        Appeal saved = appealRepository.save(appeal);

        // RN-22: toda acción de moderación registra entrada en audit_logs
        AuditLog auditLog = AuditLog.builder()
                .actor(user)
                .actionType("APPEAL_SUBMITTED")
                .entityType("APPEAL")
                .entityId(saved.getId())
                .detail("Appeal submitted for report " + report.getId())
                .build();
        auditLogRepository.save(auditLog);

        return new AppealResponse(saved);
    }

    @Override
    @Transactional
    public AppealResponse resolveAppeal(UUID appealId, String newStatus, String moderatorEmail) {
        User moderator = userRepository.findByEmail(moderatorEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + moderatorEmail));

        String roleName = moderator.getRole().getName();
        if (!"MODERATOR".equals(roleName) && !"ADMIN".equals(roleName)) {
            throw new UnauthorizedModerationException(
                    "Solo moderadores y administradores pueden resolver apelaciones");
        }

        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new ReportNotFoundException("Apelación no encontrada: " + appealId));

        if (!"OPEN".equals(appeal.getStatus())) {
            throw new AppealAlreadyResolvedException(
                    "La apelación ya fue resuelta con estado: " + appeal.getStatus());
        }

        appeal.setStatus(newStatus);
        appeal.setReviewedAt(LocalDateTime.now());
        appealRepository.save(appeal);

        AuditLog auditLog = AuditLog.builder()
                .actor(moderator)
                .actionType("APPEAL_" + newStatus)
                .entityType("APPEAL")
                .entityId(appealId)
                .detail("Appeal " + appealId + " resolved as " + newStatus)
                .build();
        auditLogRepository.save(auditLog);

        eventPublisher.publishEvent(new AppealResolvedEvent(
                appeal.getUser().getId(), appealId, newStatus));

        return new AppealResponse(appeal);
    }
}
