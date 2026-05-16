package com.mentoredu.library.controller;

import com.mentoredu.library.dto.PublishResourceRequest;
import com.mentoredu.library.dto.ResourceResponse;
import com.mentoredu.library.service.IResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Tag(name = "Biblioteca de recursos", description = "Publicación, búsqueda y consulta de recursos académicos (US12-US15).")
@SecurityRequirement(name = "bearerAuth")
public class ResourceController {

    private final IResourceService resourceService;

    @PostMapping
    @Operation(summary = "US12 - Publicar recurso académico")
    public ResponseEntity<ResourceResponse> publish(@Valid @RequestBody PublishResourceRequest request,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resourceService.publish(request, userDetails.getUsername()));
    }

    @GetMapping("/search")
    @Operation(summary = "US13 - Buscar recursos con filtros")
    public ResponseEntity<List<ResourceResponse>> search(
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String visibility) {
        return ResponseEntity.ok(resourceService.search(query, type, visibility));
    }

    @GetMapping("/{id}")
    @Operation(summary = "US14 - Obtener recurso por ID")
    public ResponseEntity<ResourceResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(resourceService.getById(id));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        HttpStatus status = ex.getMessage() != null && ex.getMessage().toLowerCase().contains("no encontrado")
                ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of("message", ex.getMessage()));
    }
}
