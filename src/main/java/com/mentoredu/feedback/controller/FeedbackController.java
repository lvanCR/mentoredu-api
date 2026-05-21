package com.mentoredu.feedback.controller;

import com.mentoredu.feedback.dto.FeedbackRequest;
import com.mentoredu.feedback.dto.FeedbackResponse;
import com.mentoredu.feedback.service.IFeedbackService;
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

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
@Tag(name = "Feedback", description = "Gestión de retroalimentación académica formal (EP-11).")
@SecurityRequirement(name = "bearerAuth")
public class FeedbackController {

    private final IFeedbackService feedbackService;

    // -------------------------------------------------------------------------
    // US34 — Provide academic feedback to student
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(
        summary = "US34 - Registrar retroalimentación académica",
        description = "Permite a un docente o administrador registrar retroalimentación académica formal "
            + "sobre el desempeño de un estudiante. "
            + "El cuerpo del mensaje es obligatorio (RN-36). "
            + "La puntuación es opcional (rango 0.0–20.0). "
            + "Solo usuarios con rol TEACHER, ACADEMY o ADMIN pueden emitir retroalimentación (RN-36). "
            + "Las entradas son inmutables una vez registradas (RN-37). "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<FeedbackResponse> provideFeedback(
            @Valid @RequestBody FeedbackRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedbackService.provideFeedback(auth.getName(), request));
    }

    // -------------------------------------------------------------------------
    // US35 — View received academic feedback
    // -------------------------------------------------------------------------

    @GetMapping("/me")
    @Operation(
        summary = "US35 - Consultar retroalimentación recibida",
        description = "Devuelve el historial de retroalimentación académica recibida por el usuario autenticado, "
            + "ordenado por fecha descendente (más reciente primero). "
            + "Si no hay retroalimentación registrada, devuelve lista vacía sin error (RN-38). "
            + "Las entradas son de solo lectura: no se pueden modificar ni eliminar (RN-37, RN-38). "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<List<FeedbackResponse>> getReceivedFeedback() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(feedbackService.getReceivedFeedback(auth.getName()));
    }

    // -------------------------------------------------------------------------
    // F1.2 — View given academic feedback (for teachers/academies)
    // -------------------------------------------------------------------------

    @GetMapping("/given")
    @Operation(
        summary = "F1.2 - Consultar retroalimentación emitida",
        description = "Devuelve el historial de retroalimentación académica emitida por el usuario autenticado "
            + "(docente, academia o admin), ordenado por fecha descendente. "
            + "Si no hay entradas, devuelve lista vacía. "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<List<FeedbackResponse>> getGivenFeedback() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(feedbackService.getGivenFeedback(auth.getName()));
    }
}
