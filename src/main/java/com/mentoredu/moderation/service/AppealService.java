package com.mentoredu.moderation.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.forum.exception.UserNotFoundException;
import com.mentoredu.moderation.dto.AppealRequest;
import com.mentoredu.moderation.dto.AppealResponse;
import com.mentoredu.moderation.exception.DuplicateAppealException;
import com.mentoredu.moderation.exception.ReportNotFoundException;
import com.mentoredu.moderation.model.Appeal;
import com.mentoredu.moderation.model.AuditLog;
import com.mentoredu.moderation.model.Report;
import com.mentoredu.moderation.repository.AppealRepository;
import com.mentoredu.moderation.repository.AuditLogRepository;
import com.mentoredu.moderation.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppealService implements IAppealService {

    private final AppealRepository appealRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

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
}
