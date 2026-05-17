package com.mentoredu.library.controller;

import com.mentoredu.library.dto.PublishResourceRequest;
import com.mentoredu.library.dto.ResourceResponse;
import com.mentoredu.library.service.IResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
            + "No se permite registrar dos recursos con el mismo fileId (RN-14). Requiere autenticación JWT."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ResourceResponse> publish(@Valid @RequestBody PublishResourceRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resourceService.publish(request, auth.getName()));
    }

    // -------------------------------------------------------------------------
    // US14 — Search resources by filters
    // -------------------------------------------------------------------------

    @GetMapping("/search")
    @Operation(
        summary = "US14 - Buscar recursos académicos por filtros",
        description = "Busca recursos académicos aplicando filtros opcionales. "
            + "No requiere autenticación — los recursos PRIVATE nunca aparecen en los resultados. "
            + "Todos los filtros son opcionales y combinables. "
            + "Filtros disponibles: q (texto en título), type, visibility, institutionId, subjectId, year. "
            + "Valores válidos para type: EXAM, SOLUTION, NOTES, PRACTICE, VIDEO, OTHER. "
            + "Valores válidos para visibility: PUBLIC, PREMIUM (PRIVATE excluido del buscador). "
            + "El año debe estar en el rango 1900–2099. "
            + "Devuelve lista vacía si no hay coincidencias (sin error)."
    )
    public ResponseEntity<List<ResourceResponse>> search(
            @Parameter(description = "Texto a buscar en el título del recurso")
            @RequestParam(required = false, name = "q") String query,

            @Parameter(description = "Tipo de recurso: EXAM, SOLUTION, NOTES, PRACTICE, VIDEO, OTHER")
            @RequestParam(required = false) String type,

            @Parameter(description = "Visibilidad: PUBLIC o PREMIUM")
            @RequestParam(required = false) String visibility,

            @Parameter(description = "UUID de la institución")
            @RequestParam(required = false) UUID institutionId,

            @Parameter(description = "UUID de la materia/curso")
            @RequestParam(required = false) UUID subjectId,

            @Parameter(description = "Año del examen (1900–2099)")
            @RequestParam(required = false) Integer year) {

        return ResponseEntity.ok(
                resourceService.search(query, type, visibility, institutionId, subjectId, year));
    }

    // -------------------------------------------------------------------------
    // US15 — Get resource by ID (skeleton)
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    @Operation(summary = "US15 - Obtener recurso por ID")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ResourceResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(resourceService.getById(id));
    }
}
