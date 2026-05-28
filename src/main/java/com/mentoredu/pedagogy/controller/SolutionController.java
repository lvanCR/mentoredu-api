package com.mentoredu.pedagogy.controller;

import com.mentoredu.config.SecurityUtils;
import com.mentoredu.pedagogy.dto.MySolutionWithFeedbackResponse;
import com.mentoredu.pedagogy.dto.SolutionResponse;
import com.mentoredu.pedagogy.dto.SubmitSolutionRequest;
import com.mentoredu.pedagogy.service.ISolutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Tag(name = "Pedagogía — Resoluciones", description = "Enviar y consultar resoluciones de ejercicios (US17, US18, US20).")
@SecurityRequirement(name = "bearerAuth")
public class SolutionController {

    private final ISolutionService solutionService;

    @PostMapping("/{resourceId}/solutions")
    @ResponseStatus(HttpStatus.CREATED)
    @io.swagger.v3.oas.annotations.Operation(summary = "US18 - Enviar resolución a un ejercicio")
    public ResponseEntity<SolutionResponse> submit(
            @PathVariable UUID resourceId,
            @Valid @RequestBody SubmitSolutionRequest request) {
        SolutionResponse response = solutionService.submit(resourceId, request, SecurityUtils.currentEmail());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/api/v1/resources/{resourceId}/solutions/mine")
                .buildAndExpand(resourceId).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{resourceId}/solutions/mine")
    @io.swagger.v3.oas.annotations.Operation(summary = "US20 - Ver mi resolución con feedback recibido")
    public ResponseEntity<MySolutionWithFeedbackResponse> getMine(@PathVariable UUID resourceId) {
        return ResponseEntity.ok(solutionService.getMyWithFeedback(resourceId, SecurityUtils.currentEmail()));
    }

    @GetMapping("/{resourceId}/solutions")
    @io.swagger.v3.oas.annotations.Operation(summary = "US17 - Listar resoluciones de un ejercicio")
    public ResponseEntity<List<SolutionResponse>> list(@PathVariable UUID resourceId) {
        return ResponseEntity.ok(solutionService.listByResource(resourceId, SecurityUtils.currentEmail()));
    }

    @GetMapping("/{resourceId}/solutions/{solutionId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "US17 - Ver detalle de una resolución específica")
    public ResponseEntity<SolutionResponse> getById(
            @PathVariable UUID resourceId,
            @PathVariable UUID solutionId) {
        return ResponseEntity.ok(solutionService.getById(resourceId, solutionId, SecurityUtils.currentEmail()));
    }
}
