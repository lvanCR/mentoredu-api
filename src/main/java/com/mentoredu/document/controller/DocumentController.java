package com.mentoredu.document.controller;

import com.mentoredu.document.dto.DuplicateDocumentResponse;
import com.mentoredu.document.dto.DocumentResponse;
import com.mentoredu.document.exception.DuplicateDocumentException;
import com.mentoredu.document.model.Document;
import com.mentoredu.document.service.IDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final IDocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentResponse> publish(@Valid @RequestBody Document document,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        DocumentResponse created = documentService.publish(document, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadPdf(@RequestParam(required = false) MultipartFile file,
                                                      @RequestParam(required = false) String title,
                                                      @RequestParam(required = false) String type,
                                                      @RequestParam(required = false) String category,
                                                      @RequestParam(required = false) String university,
                                                      @RequestParam(required = false) Integer year,
                                                      @RequestParam(required = false) String area,
                                                      @RequestParam(required = false, defaultValue = "false") Boolean confirmVersion,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        DocumentResponse created = documentService.uploadPdf(
                file,
                title,
                type,
                category,
                university,
                year,
                area,
                confirmVersion,
                userDetails.getUsername()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<DocumentResponse> download(@PathVariable Long id,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        DocumentResponse document = documentService.download(id, userDetails.getUsername());
        return ResponseEntity.ok(document);
    }

    @PatchMapping("/{id}/anonymous")
    public ResponseEntity<DocumentResponse> toggleAnonymous(@PathVariable Long id,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        DocumentResponse document = documentService.toggleAnonymous(id, userDetails.getUsername());
        return ResponseEntity.ok(document);
    }

    @ExceptionHandler(DuplicateDocumentException.class)
    public ResponseEntity<DuplicateDocumentResponse> handleDuplicateDocument(DuplicateDocumentException ex) {
        DuplicateDocumentResponse response = new DuplicateDocumentResponse(
                ex.getMessage(),
                ex.getDuplicates().stream().map(DocumentResponse::new).toList()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
}
