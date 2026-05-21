package com.mentoredu.moderation.controller;

import com.mentoredu.moderation.dto.AppealRequest;
import com.mentoredu.moderation.dto.AppealResponse;
import com.mentoredu.moderation.service.IAppealService;
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
@RequestMapping("/api/v1/moderation/appeals")
@RequiredArgsConstructor
@Tag(name = "Moderación", description = "Gestión de reportes y resolución de incidencias (EP-06).")
@SecurityRequirement(name = "bearerAuth")
public class AppealController {

    private final IAppealService appealService;

    // -------------------------------------------------------------------------
    // US38 — Submit appeal for moderation decision
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(
        summary = "US38 - Presentar apelación de moderación",
        description = "Permite a un usuario autenticado presentar una apelación sobre un reporte existente. "
            + "El campo reportId debe corresponder a un reporte registrado en el sistema. "
            + "El campo reason es obligatorio (RN-44). "
            + "Solo puede existir una apelación activa por usuario por reporte (RN-43). "
            + "Toda apelación queda registrada en audit_logs (RN-22). "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<AppealResponse> submit(@Valid @RequestBody AppealRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appealService.submit(request, auth.getName()));
    }
}
