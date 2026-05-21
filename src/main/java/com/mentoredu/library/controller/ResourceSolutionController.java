package com.mentoredu.library.controller;

import com.mentoredu.library.dto.SolutionResponse;
import com.mentoredu.library.dto.SubmitSolutionRequest;
import com.mentoredu.library.service.IResourceSolutionService;
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
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Tag(name = "Resoluciones de recursos", description = "Envío de resoluciones académicas por estudiantes (US39).")
public class ResourceSolutionController {

    private final IResourceSolutionService solutionService;

    // -------------------------------------------------------------------------
    // US39 — Submit solution to academic resource
    // -------------------------------------------------------------------------

    @PostMapping("/{resourceId}/solutions")
    @Operation(
        summary = "US39 - Enviar resolución de un recurso académico",
        description = "Permite a un estudiante enviar su resolución (PDF previamente subido) "
            + "para un recurso académico con allows_solutions=true. "
            + "Solo puede existir una resolución por par (resource_id, student_id) (RN-45). "
            + "El recurso debe tener allows_solutions=true; de lo contrario se devuelve 403 (RN-47). "
            + "Otorga 3 puntos de experiencia al estudiante (US30). "
            + "Concede la insignia FIRST_SOLUTION si es la primera resolución enviada. "
            + "Requiere autenticación JWT."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<SolutionResponse> submitSolution(
            @PathVariable UUID resourceId,
            @Valid @RequestBody SubmitSolutionRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SolutionResponse response = solutionService.submitSolution(resourceId, request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
