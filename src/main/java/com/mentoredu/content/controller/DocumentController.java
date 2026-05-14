package com.mentoredu.content.controller;

import com.mentoredu.content.dto.DocumentResponse;
import com.mentoredu.content.dto.DocumentSearchResponse;
import com.mentoredu.content.dto.DocumentViewerResponse;
import com.mentoredu.content.dto.DownloadDocumentResponse;
import com.mentoredu.content.dto.DuplicateDocumentResponse;
import com.mentoredu.content.dto.PublishDocumentRequest;
import com.mentoredu.content.exception.DailyDownloadLimitExceededException;
import com.mentoredu.content.exception.DuplicateDocumentException;
import com.mentoredu.content.exception.PdfPreviewException;
import com.mentoredu.content.service.IDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Tag(name = "Repositorio de documentos", description = "Subida, búsqueda, visualización, descarga y gestión de exámenes y materiales de estudio.")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final IDocumentService documentService;

    @PostMapping
    @Operation(summary = "Publicar documento por URL", description = "Registra un documento ya alojado en una URL externa.")
    @ApiResponse(responseCode = "201", description = "Documento registrado")
    public ResponseEntity<DocumentResponse> publish(@Valid @RequestBody PublishDocumentRequest request,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.publish(request, userDetails.getUsername()));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "US06 - Subir PDF con metadatos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Documento disponible",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Campos obligatorios incompletos o archivo inválido"),
            @ApiResponse(responseCode = "409", description = "Documento duplicado",
                    content = @Content(schema = @Schema(implementation = DuplicateDocumentResponse.class)))
    })
    public ResponseEntity<DocumentResponse> uploadPdf(
            @Parameter(description = "Archivo PDF < 10MB") @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String university,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String area,
            @RequestParam(required = false, defaultValue = "false") Boolean confirmVersion,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                documentService.uploadPdf(file, title, type, category, university, year, area,
                        confirmVersion, userDetails.getUsername()));
    }

    @GetMapping("/search")
    @Operation(summary = "US08 - Buscar documentos con filtros")
    @ApiResponse(responseCode = "200", description = "Resultado de búsqueda",
            content = @Content(schema = @Schema(implementation = DocumentSearchResponse.class)))
    public ResponseEntity<DocumentSearchResponse> search(
            @RequestParam(required = false) String university,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String area,
            @RequestParam(required = false, name = "q") String query) {
        return ResponseEntity.ok(documentService.search(university, year, area, query));
    }

    @GetMapping("/{id}/viewer")
    @Operation(summary = "US09 - Obtener configuración del visor PDF")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuración del visor",
                    content = @Content(schema = @Schema(implementation = DocumentViewerResponse.class))),
            @ApiResponse(responseCode = "422", description = "No se puede previsualizar el archivo")
    })
    public ResponseEntity<DocumentViewerResponse> viewer(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.getViewer(id));
    }

    @GetMapping("/{id}/preview")
    @Operation(summary = "US09 - Previsualizar PDF inline")
    public ResponseEntity<?> preview(@PathVariable UUID id,
                                     @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) {
        try {
            Path previewPath = documentService.getPreviewPath(id);
            UrlResource resource = new UrlResource(previewPath.toUri());
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.setContentDisposition(ContentDisposition.inline()
                    .filename(previewPath.getFileName().toString()).build());

            if (rangeHeader != null) {
                HttpRange range = HttpRange.parseRanges(rangeHeader).getFirst();
                ResourceRegion region = range.toResourceRegion(resource);
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .headers(headers).contentType(MediaType.APPLICATION_PDF).body(region);
            }
            return ResponseEntity.ok().headers(headers)
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.APPLICATION_PDF).body(resource);
        } catch (MalformedURLException ex) {
            throw new PdfPreviewException();
        } catch (PdfPreviewException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new PdfPreviewException();
        }
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "US07 - Descargar documento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Descarga iniciada",
                    content = @Content(schema = @Schema(implementation = DownloadDocumentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Saldo insuficiente"),
            @ApiResponse(responseCode = "429", description = "Límite diario alcanzado")
    })
    public ResponseEntity<DownloadDocumentResponse> download(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean useCoins,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(documentService.download(id, userDetails.getUsername(), useCoins));
    }

    @PatchMapping("/{id}/anonymous")
    @Operation(summary = "Alternar anonimato del documento")
    @ApiResponse(responseCode = "200", description = "Anonimato actualizado",
            content = @Content(schema = @Schema(implementation = DocumentResponse.class)))
    public ResponseEntity<DocumentResponse> toggleAnonymous(@PathVariable UUID id,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(documentService.toggleAnonymous(id, userDetails.getUsername()));
    }

    @ExceptionHandler(DuplicateDocumentException.class)
    public ResponseEntity<DuplicateDocumentResponse> handleDuplicate(DuplicateDocumentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new DuplicateDocumentResponse(ex.getMessage(),
                        ex.getDuplicates().stream().map(DocumentResponse::new).toList()));
    }

    @ExceptionHandler(DailyDownloadLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handleDailyLimit(DailyDownloadLimitExceededException ex) {
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

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        HttpStatus status = ex.getMessage() != null && ex.getMessage().toLowerCase().contains("no encontrado")
                ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of("message", ex.getMessage()));
    }
}
