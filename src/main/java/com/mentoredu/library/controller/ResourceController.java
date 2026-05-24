package com.mentoredu.library.controller;

import com.mentoredu.config.PagedResponse;
import com.mentoredu.library.dto.DownloadResponse;
import com.mentoredu.library.dto.PublishResourceRequest;
import com.mentoredu.library.dto.ResourceResponse;
import com.mentoredu.library.dto.UpdateResourceSettingsRequest;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Tag(name = "Biblioteca de recursos", description = "Publicación, búsqueda y consulta de recursos académicos.")
@SecurityRequirement(name = "bearerAuth")
public class ResourceController {

    private final IResourceService resourceService;

    private Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean isUnauthenticated(Authentication a) {
        return a == null || !a.isAuthenticated() || a instanceof AnonymousAuthenticationToken;
    }

    // -------------------------------------------------------------------------
    // POST /resources — Publish resource (US07+US08)
    // -------------------------------------------------------------------------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "US08 - Registrar recurso académico",
        description = "Publica un recurso académico. El campo fileUrl debe provenir de POST /resources/files. "
            + "Tipos válidos: EXAMEN, GUIA, APUNTES, PRACTICA, OTRO. "
            + "aceptaResoluciones solo puede ser true para TEACHER, ACADEMY o ADMIN (RN-14)."
    )
    public ResponseEntity<ResourceResponse> publish(@Valid @RequestBody PublishResourceRequest request) {
        Authentication a = auth();
        if (isUnauthenticated(a)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resourceService.publish(request, a.getName()));
    }

    // -------------------------------------------------------------------------
    // GET /resources/search — Search resources (US09)
    // -------------------------------------------------------------------------

    @GetMapping
    @Operation(
        summary = "US09 - Buscar recursos académicos por filtros",
        description = "Todos los filtros son opcionales. careerId filtra por carrera específica dentro del área. "
            + "Recursos PRIVATE nunca aparecen en resultados. Soporta paginación con page (default 0) y size (default 20)."
    )
    public ResponseEntity<PagedResponse<ResourceResponse>> search(
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false)              String type,
            @RequestParam(required = false)              UUID universityId,
            @RequestParam(required = false)              UUID areaId,
            @RequestParam(required = false)              UUID careerId,
            @RequestParam(required = false)              UUID courseId,
            @RequestParam(defaultValue = "0")            int page,
            @RequestParam(defaultValue = "20")           int size) {

        return ResponseEntity.ok(resourceService.search(query, type, universityId, areaId, careerId, courseId, page, size));
    }

    // -------------------------------------------------------------------------
    // GET /resources/{id} — Get resource by ID
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    @Operation(summary = "US09 - Obtener metadatos de un recurso por ID")
    public ResponseEntity<ResourceResponse> getById(@PathVariable UUID id) {
        Authentication a = auth();
        if (isUnauthenticated(a)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(resourceService.getById(id));
    }

    // -------------------------------------------------------------------------
    // GET /resources/me — Get my published resources (US11)
    // -------------------------------------------------------------------------

    @GetMapping("/me")
    @Operation(summary = "US11 - Ver mis recursos publicados")
    public ResponseEntity<PagedResponse<ResourceResponse>> getMyResources(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Authentication a = auth();
        if (isUnauthenticated(a)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(resourceService.getByAuthor(a.getName(), page, size));
    }

    // -------------------------------------------------------------------------
    // PATCH /resources/{id}/settings — Update resource settings (US16 Escenario 2)
    // -------------------------------------------------------------------------

    @PatchMapping("/{id}/settings")
    @Operation(summary = "US16 - Activar o desactivar acepta_resoluciones en un recurso",
               description = "Permite al autor activar o desactivar acepta_resoluciones. Solo válido para PRACTICA (RN-08). STUDENT → 403 (RN-05).")
    public ResponseEntity<ResourceResponse> updateSettings(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateResourceSettingsRequest request) {
        Authentication a = auth();
        if (isUnauthenticated(a)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(resourceService.updateSettings(id, request, a.getName()));
    }

    // -------------------------------------------------------------------------
    // GET /resources/{id}/download — Download resource (US10)
    // -------------------------------------------------------------------------

    @GetMapping("/{id}/download")
    @Operation(summary = "US10 - Descargar un recurso académico")
    public ResponseEntity<DownloadResponse> download(@PathVariable UUID id) {
        Authentication a = auth();
        if (isUnauthenticated(a)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(resourceService.download(id, a.getName()));
    }
}
