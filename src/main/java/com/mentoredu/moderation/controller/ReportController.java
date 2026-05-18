package com.mentoredu.moderation.controller;

import com.mentoredu.moderation.dto.ReportRequest;
import com.mentoredu.moderation.dto.ReportResponse;
import com.mentoredu.moderation.dto.ResolveReportRequest;
import com.mentoredu.moderation.dto.ResolveReportResponse;
import com.mentoredu.moderation.service.IReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/moderation/reports")
@RequiredArgsConstructor
@Tag(name = "Moderación", description = "Gestión de reportes y resolución de incidencias (EP-06).")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final IReportService reportService;

    // -------------------------------------------------------------------------
    // US19 — Report content
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(
        summary = "US19 - Reportar contenido",
        description = "Registra un reporte sobre un hilo, respuesta, comentario o recurso. "
            + "El campo targetType acepta: THREAD, ANSWER, COMMENT, RESOURCE. "
            + "El campo reason es obligatorio (RN-20). "
            + "No se permite reportar contenido propio ni duplicar un reporte existente. "
            + "Toda acción de moderación queda registrada en audit_logs (RN-22). "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<ReportResponse> create(@Valid @RequestBody ReportRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.create(request, auth.getName()));
    }

    // -------------------------------------------------------------------------
    // US20 — Resolve report
    // -------------------------------------------------------------------------

    @PatchMapping("/{id}/resolve")
    @Operation(
        summary = "US20 - Resolver reporte",
        description = "Permite a un moderador o administrador resolver un reporte pendiente. "
            + "El campo resolution acepta: RESOLVED, REJECTED. "
            + "El campo actionType acepta: HIDE, DELETE, WARN, SUSPEND, RESTORE. "
            + "Solo usuarios con rol MODERATOR o ADMIN pueden ejecutar esta operación (RN-21). "
            + "Un reporte ya resuelto o rechazado no puede modificarse nuevamente. "
            + "Toda resolución queda registrada en moderation_actions y audit_logs (RN-22). "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<ResolveReportResponse> resolve(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveReportRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(reportService.resolve(id, request, auth.getName()));
    }
}
