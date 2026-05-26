package com.mentoredu.community.controller;

import com.mentoredu.config.PagedResponse;
import com.mentoredu.community.dto.ReportRequest;
import com.mentoredu.community.dto.ReportResponse;
import com.mentoredu.community.dto.ResolveReportRequest;
import com.mentoredu.community.service.IReportService;
import com.mentoredu.config.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/moderation/reports")
@RequiredArgsConstructor
@Tag(name = "Moderación - Reportes", description = "Reportar contenido y resolver reportes (US25, US26)")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final IReportService reportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "US25 - Reportar contenido inapropiado")
    public ResponseEntity<ReportResponse> create(@Valid @RequestBody ReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.create(request, SecurityUtils.currentEmail()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @Operation(summary = "US26 - Listar reportes abiertos (MODERATOR/ADMIN)")
    public ResponseEntity<PagedResponse<ReportResponse>> listOpen(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        SecurityUtils.requireAnyRole("MODERATOR", "ADMIN");
        return ResponseEntity.ok(reportService.listOpen(page, size));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @Operation(summary = "US26 - Resolver un reporte de moderación")
    public ResponseEntity<ReportResponse> resolve(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveReportRequest request) {
        SecurityUtils.requireAnyRole("MODERATOR", "ADMIN");
        return ResponseEntity.ok(reportService.resolve(id, request, SecurityUtils.currentEmail()));
    }
}
