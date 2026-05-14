package com.mentoredu.moderation.service;

import com.mentoredu.auth.model.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.content.model.AcademicResource;
import com.mentoredu.content.repository.AcademicResourceRepository;
import com.mentoredu.moderation.dto.ReportRequest;
import com.mentoredu.moderation.dto.ReportResponse;
import com.mentoredu.moderation.model.Report;
import com.mentoredu.moderation.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService implements IReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AcademicResourceRepository academicResourceRepository;

    @Override
    public ReportResponse create(ReportRequest request, String reporterEmail) {
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        AcademicResource resource = academicResourceRepository.findById(request.getTargetId())
                .orElseThrow(() -> new RuntimeException("Recurso no encontrado"));

        if (resource.getAuthor().getId().equals(reporter.getId())) {
            throw new RuntimeException("No puedes reportar tu propio recurso");
        }

        if (reportRepository.existsByReportedByIdAndTargetTypeAndTargetId(
                reporter.getId(), request.getTargetType(), request.getTargetId())) {
            throw new RuntimeException("Ya reportaste este contenido");
        }

        Report report = Report.builder()
                .reportedBy(reporter)
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .reason(request.getReason())
                .build();

        return new ReportResponse(reportRepository.save(report));
    }
}
