package com.mentoredu.library.controller;

import com.mentoredu.config.SecurityUtils;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resources/files")
@RequiredArgsConstructor
@Tag(name = "Archivos de recursos", description = "Subida de archivos PDF académicos (US07).")
@SecurityRequirement(name = "bearerAuth")
public class ResourceFileController {

    private final IResourceFileService resourceFileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "US07 - Subir archivo PDF académico",
        description = "Sube un archivo PDF al servidor y genera su referencia interna. "
            + "Solo se aceptan archivos con tipo MIME application/pdf y tamaño máximo configurable (default 20 MB). "
            + "Los campos devueltos (fileUrl, fileName, mimeType, sizeBytes) deben copiarse al body de US08 para registrar los metadatos. "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<ResourceFileResponse> upload(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Archivo PDF a subir",
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("file") MultipartFile file) {
        // currentEmail() validates authentication — SecurityConfig also enforces anyRequest().authenticated()
        SecurityUtils.currentEmail();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resourceFileService.upload(file));
    }
}
