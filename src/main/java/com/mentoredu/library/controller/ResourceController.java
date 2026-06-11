package com.mentoredu.library.controller;

import com.mentoredu.config.PagedResponse;
import com.mentoredu.config.SecurityUtils;
import com.mentoredu.library.dto.DownloadResponse;
import com.mentoredu.library.dto.PublishResourceRequest;
import com.mentoredu.library.dto.ResourceResponse;
import com.mentoredu.library.dto.UpdateResourceSettingsRequest;
import com.mentoredu.library.service.IResourceFileService;
import com.mentoredu.library.service.IResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Tag(name = "Biblioteca de recursos", description = "Publicación, búsqueda y consulta de recursos académicos.")
@SecurityRequirement(name = "bearerAuth")
public class ResourceController {

    private final IResourceService resourceService;
    private final IResourceFileService resourceFileService;

    @PostMapping
    @Operation(
        summary = "US08 - Registrar recurso académico",
        description = "Publica un recurso académico. El campo fileUrl debe provenir de POST /resources/files. "
            + "Tipos válidos: EXAMEN, GUIA, APUNTES, PRACTICA, OTRO. "
            + "aceptaResoluciones solo puede ser true para TEACHER, ACADEMY o ADMIN (RN-14)."
    )
    public ResponseEntity<ResourceResponse> publish(@Valid @RequestBody PublishResourceRequest request) {
        ResourceResponse response = resourceService.publish(request, SecurityUtils.currentEmail());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(location).body(response);
    }

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
            @RequestParam(required = false)              UUID authorId,
            @RequestParam(defaultValue = "0")            int page,
            @RequestParam(defaultValue = "20")           int size) {
        return ResponseEntity.ok(resourceService.search(query, type, universityId, areaId, careerId, courseId, authorId, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "US09 - Obtener metadatos de un recurso por ID")
    public ResponseEntity<ResourceResponse> getById(@PathVariable UUID id) {
        SecurityUtils.requireAuth();
        return ResponseEntity.ok(resourceService.getById(id));
    }

    @GetMapping("/me")
    @Operation(summary = "US11 - Ver mis recursos publicados")
    public ResponseEntity<PagedResponse<ResourceResponse>> getMyResources(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(resourceService.getByAuthor(SecurityUtils.currentEmail(), page, size));
    }

    @PatchMapping("/{id}/settings")
    @Operation(summary = "US16 - Activar o desactivar acepta_resoluciones en un recurso",
               description = "Permite al autor activar o desactivar acepta_resoluciones. Solo válido para PRACTICA (RN-08). STUDENT → 403 (RN-05).")
    public ResponseEntity<ResourceResponse> updateSettings(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateResourceSettingsRequest request) {
        return ResponseEntity.ok(resourceService.updateSettings(id, request, SecurityUtils.currentEmail()));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "US10 - Descargar un recurso académico")
    public ResponseEntity<DownloadResponse> download(@PathVariable UUID id) {
        return ResponseEntity.ok(resourceService.download(id, SecurityUtils.currentEmail()));
    }

    @GetMapping("/{id}/content")
    @Operation(summary = "US10 - Obtener contenido binario de un recurso (proxy)")
    public ResponseEntity<byte[]> content(@PathVariable UUID id) {
        SecurityUtils.requireAuth();
        ResourceResponse resource = resourceService.getById(id);
        byte[] bytes = resourceFileService.fetchContent(resource.getFileUrl());
        String mimeType = resource.getMimeType() != null ? resource.getMimeType() : "application/octet-stream";
        String fileName = resource.getFileName() != null ? resource.getFileName() : "file";
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(mimeType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
            .body(bytes);
    }
}
