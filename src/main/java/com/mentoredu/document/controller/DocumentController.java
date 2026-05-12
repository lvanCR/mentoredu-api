package com.mentoredu.document.controller;

import com.mentoredu.document.dto.DocumentResponse;
import com.mentoredu.document.dto.DocumentSearchResponse;
import com.mentoredu.document.dto.DocumentViewerResponse;
import com.mentoredu.document.dto.DownloadDocumentResponse;
import com.mentoredu.document.dto.DuplicateDocumentResponse;
import com.mentoredu.document.exception.DailyDownloadLimitExceededException;
import com.mentoredu.document.exception.DuplicateDocumentException;
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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/search")
    public ResponseEntity<DocumentSearchResponse> search(@RequestParam(required = false) String university,
                                                         @RequestParam(required = false) Integer year,
                                                         @RequestParam(required = false) String area,
                                                         @RequestParam(required = false, name = "q") String query) {
        return ResponseEntity.ok(documentService.search(university, year, area, query));
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

    @ExceptionHandler(DuplicateDocumentException.class)
    public ResponseEntity<DuplicateDocumentResponse> handleDuplicateDocument(DuplicateDocumentException ex) {
        DuplicateDocumentResponse response = new DuplicateDocumentResponse(
                ex.getMessage(),
                ex.getDuplicates().stream().map(DocumentResponse::new).toList()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(DailyDownloadLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handleDailyDownloadLimit(DailyDownloadLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(PdfPreviewException.class)
    public ResponseEntity<Map<String, String>> handlePdfPreview(PdfPreviewException ex) {
        return ResponseEntity.unprocessableEntity().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
}
