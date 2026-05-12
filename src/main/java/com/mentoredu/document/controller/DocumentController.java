package com.mentoredu.document.controller;

import com.mentoredu.document.dto.DocumentResponse;
import com.mentoredu.document.dto.DocumentViewerResponse;
import com.mentoredu.document.exception.PdfPreviewException;
import com.mentoredu.document.model.Document;
import com.mentoredu.document.service.IDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
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

    @GetMapping("/{id}/viewer")
    public ResponseEntity<DocumentViewerResponse> viewer(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getViewer(id));
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<?> preview(@PathVariable Long id,
                                     @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) {
        try {
            Path previewPath = documentService.getPreviewPath(id);
            UrlResource resource = new UrlResource(previewPath.toUri());
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.setContentDisposition(ContentDisposition.inline()
                    .filename(previewPath.getFileName().toString())
                    .build());

            if (rangeHeader != null) {
                HttpRange range = HttpRange.parseRanges(rangeHeader).getFirst();
                ResourceRegion region = range.toResourceRegion(resource);
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(region);
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (MalformedURLException ex) {
            throw new PdfPreviewException();
        } catch (Exception ex) {
            if (ex instanceof PdfPreviewException) {
                throw (PdfPreviewException) ex;
            }
            throw new PdfPreviewException();
        }
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

    @ExceptionHandler(PdfPreviewException.class)
    public ResponseEntity<Map<String, String>> handlePdfPreview(PdfPreviewException ex) {
        return ResponseEntity.unprocessableEntity().body(Map.of("message", ex.getMessage()));
    }
}
