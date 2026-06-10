package com.mentoredu.files.controller;

import com.mentoredu.config.SecurityUtils;
import com.mentoredu.files.dto.ImageFileResponse;
import com.mentoredu.files.service.IImageFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "Archivos de imágenes", description = "Subida de imágenes PNG/JPEG para avatars y documentos de verificación")
@SecurityRequirement(name = "bearerAuth")
public class ImageFileController {

    private final IImageFileService imageFileService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Subir imagen (PNG o JPEG, máx 5 MB)",
        description = "Devuelve la fileUrl que se usa como avatarUrl en PATCH /profiles/me " +
            "o como fileUrl en POST /verification/requests."
    )
    public ResponseEntity<ImageFileResponse> upload(@RequestParam("file") MultipartFile file) {
        SecurityUtils.currentEmail();
        return ResponseEntity.status(HttpStatus.CREATED).body(imageFileService.upload(file));
    }
}
