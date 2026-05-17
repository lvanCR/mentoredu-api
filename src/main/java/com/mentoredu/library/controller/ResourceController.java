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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Tag(name = "Biblioteca de recursos", description = "Publicación, búsqueda y consulta de recursos académicos (US13-US15).")
@SecurityRequirement(name = "bearerAuth")
public class ResourceController {

    private final IResourceService resourceService;

    // -------------------------------------------------------------------------
    // US13 — Register resource metadata
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(
        summary = "US13 - Registrar metadatos del recurso académico",
        description = "Registra los metadatos de un recurso académico previamente subido (US12). "
            + "El campo fileId debe referenciar el id devuelto por POST /api/v1/resources/files. "
            + "Campos obligatorios (RN-12): title, fileId, institutionId, subjectId, year, type. "
            + "Valores válidos para type: EXAM, SOLUTION, NOTES, PRACTICE, VIDEO, OTHER. "
            + "Valores válidos para visibility: PUBLIC (default), PREMIUM, PRIVATE. "
            + "No se permite registrar dos recursos con el mismo fileId (RN-14)."
    )
    public ResponseEntity<ResourceResponse> publish(@Valid @RequestBody PublishResourceRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resourceService.publish(request, auth.getName()));
    }

    // -------------------------------------------------------------------------
    // US14 — Search resources by filters (skeleton)
    // -------------------------------------------------------------------------

    @GetMapping("/search")
    @Operation(summary = "US14 - Buscar recursos con filtros")
    public ResponseEntity<List<ResourceResponse>> search(
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String visibility) {
        return ResponseEntity.ok(resourceService.search(query, type, visibility));
    }

    // -------------------------------------------------------------------------
    // US15 — Get resource by ID (skeleton)
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    @Operation(summary = "US15 - Obtener recurso por ID")
    public ResponseEntity<ResourceResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(resourceService.getById(id));
    }
}
