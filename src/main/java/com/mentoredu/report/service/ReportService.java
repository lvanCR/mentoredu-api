package com.mentoredu.report.service;

import com.mentoredu.auth.model.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.content.model.Document;
import com.mentoredu.content.repository.DocumentRepository;
import com.mentoredu.report.dto.ReportRequest;
import com.mentoredu.report.dto.ReportResponse;
import com.mentoredu.report.model.Report;
import com.mentoredu.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService implements IReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    @Override
    public ReportResponse create(ReportRequest request, String reporterEmail) {
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Document document = documentRepository.findById(request.getTargetId())
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        if (document.getAuthor().getId().equals(reporter.getId())) {
            throw new RuntimeException("No puedes reportar tu propio documento");
        }

        if (reportRepository.existsByReportedByIdAndTargetTypeAndTargetId(
                reporter.getId(), request.getTargetType(), request.getTargetId())) {
            throw new RuntimeException("Ya reportaste este documento");
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
