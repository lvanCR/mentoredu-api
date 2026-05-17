package com.mentoredu.library.controller;

import com.mentoredu.library.dto.ResourceFileResponse;
import com.mentoredu.library.service.IResourceFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resources/files")
@RequiredArgsConstructor
@Tag(name = "Archivos de recursos", description = "Subida de archivos PDF académicos (US12).")
@SecurityRequirement(name = "bearerAuth")
public class ResourceFileController {

    private final IResourceFileService resourceFileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "US12 - Subir archivo PDF académico",
        description = "Sube un archivo PDF al servidor y genera su referencia interna. "
            + "Solo se aceptan archivos con tipo MIME application/pdf y tamaño máximo configurable (default 20 MB). "
            + "El ID devuelto (id) debe usarse en US13 al registrar los metadatos del recurso (campo fileId). "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<ResourceFileResponse> upload(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Archivo PDF a subir",
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("file") MultipartFile file) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resourceFileService.upload(file));
    }
}
