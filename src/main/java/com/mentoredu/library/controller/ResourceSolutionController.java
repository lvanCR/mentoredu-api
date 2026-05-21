package com.mentoredu.library.controller;

import com.mentoredu.library.dto.MySolutionWithFeedbackResponse;
import com.mentoredu.library.dto.SolutionDetailResponse;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Tag(name = "Resoluciones de recursos", description = "Envío y consulta de resoluciones académicas (US39, F2.2).")
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

    // -------------------------------------------------------------------------
    // US41 — View my solution and received feedback
    // -------------------------------------------------------------------------

    @GetMapping("/{resourceId}/solutions/mine")
    @Operation(
        summary = "US41 - Consultar mi resolución y feedback recibido",
        description = "Devuelve la resolución enviada por el estudiante autenticado para el recurso indicado, "
            + "junto con el feedback del docente si ya fue evaluada (status=REVIEWED). "
            + "Si aún no hay feedback, devuelve status=SUBMITTED y feedback=null. "
            + "Responde 404 si el estudiante no ha enviado ninguna resolución para ese recurso. "
            + "Solo el propio estudiante puede llamar este endpoint (RN-46). "
            + "Requiere autenticación JWT."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MySolutionWithFeedbackResponse> getMySolution(
            @PathVariable UUID resourceId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(solutionService.getMySubmittedSolution(resourceId, auth.getName()));
    }

    // -------------------------------------------------------------------------
    // F2.2 — List solutions for a resource (RN-46)
    // -------------------------------------------------------------------------

    @GetMapping("/{resourceId}/solutions")
    @Operation(
        summary = "F2.2 - Listar resoluciones de un recurso",
        description = "Lista las resoluciones de un recurso académico según el rol del solicitante (RN-46). "
            + "TEACHER/ACADEMY: todas las soluciones si son autores del recurso. "
            + "STUDENT: solo su propia solución. "
            + "MODERATOR/ADMIN: todas las soluciones. "
            + "Requiere autenticación JWT."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<SolutionDetailResponse>> getSolutions(
            @PathVariable UUID resourceId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(solutionService.getSolutionsForResource(resourceId, auth.getName()));
    }

    // -------------------------------------------------------------------------
    // F2.2 — Get single solution by ID (RN-46)
    // -------------------------------------------------------------------------

    @GetMapping("/{resourceId}/solutions/{solutionId}")
    @Operation(
        summary = "F2.2 - Obtener una resolución específica",
        description = "Obtiene el detalle de una resolución específica, incluyendo la URL del archivo. "
            + "Acceso restringido según RN-46: autor de la solución, autor del recurso, moderadores y admins. "
            + "Requiere autenticación JWT."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<SolutionDetailResponse> getSolutionById(
            @PathVariable UUID resourceId,
            @PathVariable UUID solutionId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(solutionService.getSolutionById(resourceId, solutionId, auth.getName()));
    }
}
