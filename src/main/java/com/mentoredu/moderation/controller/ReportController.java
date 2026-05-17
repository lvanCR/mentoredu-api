package com.mentoredu.moderation.controller;

import com.mentoredu.moderation.dto.ReportRequest;
import com.mentoredu.moderation.dto.ReportResponse;
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
}
