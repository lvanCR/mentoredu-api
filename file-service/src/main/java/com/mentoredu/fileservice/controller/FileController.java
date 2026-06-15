package com.mentoredu.fileservice.controller;

import com.mentoredu.fileservice.dto.FileUploadResponse;
import com.mentoredu.fileservice.exception.FileSizeLimitExceededException;
import com.mentoredu.fileservice.exception.InvalidFileTypeException;
import com.mentoredu.fileservice.service.IFileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private static final String PDF_MIME              = "application/pdf";
    private static final byte[] PDF_MAGIC             = "%PDF".getBytes(StandardCharsets.US_ASCII);
    private static final Set<String> IMAGE_MIMES      = Set.of("image/png", "image/jpeg");
    private static final Map<String, byte[]> IMG_MAGIC = Map.of(
        "image/png",  new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
        "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
    );

    private final IFileStorageService fileStorageService;

    @Value("${app.file.max-pdf-size-mb:10}")
    private int maxPdfSizeMb;

    @Value("${app.file.max-image-size-mb:5}")
    private int maxImageSizeMb;

    @PostMapping(value = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<FileUploadResponse> uploadPdf(@RequestParam("file") MultipartFile file) {
        validateNotEmpty(file);
        validateMimeType(file, PDF_MIME);
        validateSize(file, maxPdfSizeMb);
        validateMagicBytes(file, PDF_MAGIC, 4);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileStorageService.store(file, "resources"));
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<FileUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        validateNotEmpty(file);
        if (!IMAGE_MIMES.contains(file.getContentType())) {
            throw new InvalidFileTypeException(
                "Solo se aceptan imágenes PNG o JPEG. Tipo recibido: " + file.getContentType());
        }
        validateSize(file, maxImageSizeMb);
        byte[] magic = IMG_MAGIC.get(file.getContentType());
        int checkLen = "image/jpeg".equals(file.getContentType()) ? 3 : 4;
        validateMagicBytes(file, magic, checkLen);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileStorageService.store(file, "images"));
    }

    private void validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new InvalidFileTypeException("No se proporcionó un archivo o está vacío.");
    }

    private void validateMimeType(MultipartFile file, String expected) {
        if (!expected.equals(file.getContentType()))
            throw new InvalidFileTypeException(
                "Tipo de archivo no válido. Esperado: " + expected + ". Recibido: " + file.getContentType());
    }

    private void validateSize(MultipartFile file, int maxMb) {
        if (file.getSize() > (long) maxMb * 1024 * 1024)
            throw new FileSizeLimitExceededException(
                "El archivo supera el tamaño máximo de " + maxMb + " MB.");
    }

    @GetMapping("/stream")
    public ResponseEntity<byte[]> stream(@RequestParam String fileUrl) {
        byte[] content = fileStorageService.fetch(fileUrl);
        String mimeType = detectMimeType(fileUrl);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(mimeType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
            .body(content);
    }

    private String detectMimeType(String url) {
        String lower = url.toLowerCase();
        if (lower.contains(".pdf"))  return "application/pdf";
        if (lower.contains(".png"))  return "image/png";
        if (lower.contains(".jpg") || lower.contains(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    private void validateMagicBytes(MultipartFile file, byte[] expected, int length) {
        try {
            byte[] first = Arrays.copyOf(file.getBytes(), length);
            byte[] magic = Arrays.copyOf(expected, length);
            if (!Arrays.equals(first, magic))
                throw new InvalidFileTypeException("El archivo está corrupto o no es del tipo declarado.");
        } catch (IOException ex) {
            throw new InvalidFileTypeException("No se pudo leer el archivo: " + ex.getMessage());
        }
    }
}
