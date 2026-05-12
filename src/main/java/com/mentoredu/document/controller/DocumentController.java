package com.mentoredu.document.controller;

import com.mentoredu.document.dto.DocumentResponse;
import com.mentoredu.document.dto.DownloadDocumentResponse;
import com.mentoredu.document.exception.DailyDownloadLimitExceededException;
import com.mentoredu.document.model.Document;
import com.mentoredu.document.service.IDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}/download")
    public ResponseEntity<DownloadDocumentResponse> download(@PathVariable Long id,
                                                             @RequestParam(defaultValue = "false") boolean useCoins,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        DownloadDocumentResponse document = documentService.download(id, userDetails.getUsername(), useCoins);
        return ResponseEntity.ok(document);
    }

    @PatchMapping("/{id}/anonymous")
    public ResponseEntity<DocumentResponse> toggleAnonymous(@PathVariable Long id,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        DocumentResponse document = documentService.toggleAnonymous(id, userDetails.getUsername());
        return ResponseEntity.ok(document);
    }

    @ExceptionHandler(DailyDownloadLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handleDailyDownloadLimit(DailyDownloadLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
}
