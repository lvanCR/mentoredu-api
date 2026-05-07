package com.mentoredu.document.controller;

import com.mentoredu.document.model.Document;
import com.mentoredu.document.service.IDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final IDocumentService documentService;

    @PostMapping
    public ResponseEntity<Document> publish(@Valid @RequestBody Document document,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        Document created = documentService.publish(document, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Document> download(@PathVariable Long id,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        Document document = documentService.download(id, userDetails.getUsername());
        return ResponseEntity.ok(document);
    }
}
