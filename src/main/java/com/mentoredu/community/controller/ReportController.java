package com.mentoredu.community.controller;

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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/moderation/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ReportResponse> create(@Valid @RequestBody ReportRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User reporter = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                reporter.getId(), request.targetType(), request.targetId()))
            throw new DuplicateReportException("Ya reportaste este contenido");
        Report report = Report.builder()
            .reporter(reporter)
            .targetType(request.targetType())
            .targetId(request.targetId())
            .reason(request.reason())
            .status("OPEN")
            .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(ReportResponse.from(reportRepository.save(report)));
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> listOpen() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!hasModeratorRole(auth)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(
            reportRepository.findByStatus("OPEN").stream().map(ReportResponse::from).toList()
        );
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<ReportResponse> resolve(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveReportRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!hasModeratorRole(auth)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new ReportNotFoundException("Reporte no encontrado: " + id));
        if ("RESOLVED".equals(report.getStatus()))
            throw new ReportAlreadyResolvedException("El reporte ya fue resuelto");
        User resolver = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        report.setStatus("RESOLVED");
        report.setResolvedBy(resolver);
        report.setResolutionNote(request.resolutionNote());
        report.setResolvedAt(LocalDateTime.now());
        return ResponseEntity.ok(ReportResponse.from(reportRepository.save(report)));
    }

    private boolean hasModeratorRole(Authentication auth) {
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_MODERATOR")
                        || a.getAuthority().equals("ROLE_ADMIN"));
    }
}
