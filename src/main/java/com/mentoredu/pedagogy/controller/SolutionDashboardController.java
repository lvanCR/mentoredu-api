package com.mentoredu.pedagogy.controller;

import com.mentoredu.config.PagedResponse;
import com.mentoredu.config.SecurityUtils;
import com.mentoredu.pedagogy.dto.MySolutionSummaryResponse;
import com.mentoredu.pedagogy.dto.ReceivedSolutionResponse;
import com.mentoredu.pedagogy.service.ISolutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/solutions")
@RequiredArgsConstructor
@Tag(name = "Pedagogía — Dashboard", description = "Historial de resoluciones del estudiante y bandeja del docente.")
@SecurityRequirement(name = "bearerAuth")
public class SolutionDashboardController {

    private final ISolutionService solutionService;

    @GetMapping("/mine")
    @Operation(summary = "Mis resoluciones — historial completo del estudiante autenticado")
    public ResponseEntity<PagedResponse<MySolutionSummaryResponse>> getMine(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(solutionService.getMySolutions(SecurityUtils.currentEmail(), page, size));
    }

    @GetMapping("/received")
    @Operation(summary = "Resoluciones recibidas — bandeja del docente/academia autenticado")
    public ResponseEntity<PagedResponse<ReceivedSolutionResponse>> getReceived(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(solutionService.getReceivedSolutions(SecurityUtils.currentEmail(), page, size));
    }
}
