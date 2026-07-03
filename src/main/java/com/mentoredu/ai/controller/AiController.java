package com.mentoredu.ai.controller;

import com.mentoredu.ai.dto.ChatRequest;
import com.mentoredu.ai.dto.ChatResponse;
import com.mentoredu.ai.dto.SolutionReportResponse;
import com.mentoredu.ai.dto.SuggestionsResponse;
import com.mentoredu.ai.service.AssistantService;
import com.mentoredu.ai.service.SupportService;
import com.mentoredu.config.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "IA — Asistente MentorEdu", description = "Módulo de inteligencia artificial: asistente, reporte y soporte con RAG.")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private final AssistantService assistantService;
    private final SupportService supportService;

    @Operation(summary = "Asistente: busca recursos académicos por lenguaje natural (Tool Calling)")
    @PostMapping("/assistant")
    public ChatResponse assistant(@RequestBody ChatRequest request) {
        return new ChatResponse(assistantService.chat(request.message()));
    }

    @Operation(summary = "Asistente: sugerencias de búsqueda basadas en recursos reales de la BD")
    @GetMapping("/assistant/suggestions")
    public SuggestionsResponse assistantSuggestions() {
        return new SuggestionsResponse(assistantService.suggestions());
    }

    @Operation(summary = "Reporte: análisis IA de resoluciones de un ejercicio (Structured Output) — solo TEACHER y ACADEMY")
    @PreAuthorize("hasAnyRole('TEACHER', 'ACADEMY')")
    @GetMapping("/report/{resourceId}")
    public SolutionReportResponse report(@PathVariable UUID resourceId) {
        return assistantService.report(resourceId, SecurityUtils.currentEmail());
    }

    @Operation(summary = "Soporte: pregunta respondida con el PDF guía de MentorEdu (RAG)")
    @PostMapping("/support/ask")
    public ChatResponse supportAsk(@RequestBody ChatRequest request) {
        return new ChatResponse(supportService.ask(request.message()));
    }

    @Operation(summary = "Soporte: ingesta el PDF guía al vector store — solo ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/support/ingest")
    public Map<String, Object> supportIngest() throws IOException {
        return Map.of("chunksIngested", supportService.ingest());
    }
}
