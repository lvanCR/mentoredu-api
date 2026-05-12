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
@Tag(name = "Repositorio de documentos", description = "Subida, búsqueda, visualización, descarga y gestión de exámenes y materiales de estudio.")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final IDocumentService documentService;

    @PostMapping
    @Operation(summary = "Publicar documento por JSON", description = "Endpoint legado para registrar documentos ya alojados por URL.")
    @ApiResponse(responseCode = "201", description = "Documento registrado")
    public ResponseEntity<DocumentResponse> publish(@Valid @RequestBody Document document,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        DocumentResponse created = documentService.publish(document, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "US06 - Subir PDF con metadatos", description = "Guarda un examen o práctica en PDF menor a 10MB con universidad, año y área obligatorios. Si detecta duplicado responde 409 y permite versionar con confirmVersion=true.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Documento disponible en el repositorio", content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Campos obligatorios incompletos o archivo inválido"),
            @ApiResponse(responseCode = "409", description = "Documento duplicado detectado", content = @Content(schema = @Schema(implementation = DuplicateDocumentResponse.class)))
    })
    public ResponseEntity<DocumentResponse> uploadPdf(@Parameter(description = "Archivo PDF válido menor a 10MB") @RequestParam(required = false) MultipartFile file,
                                                      @Parameter(description = "Título visible del documento") @RequestParam(required = false) String title,
                                                      @Parameter(description = "Tipo de material. Por defecto PDF") @RequestParam(required = false) String type,
                                                      @Parameter(description = "Categoría del documento. Por defecto el área") @RequestParam(required = false) String category,
                                                      @Parameter(description = "Universidad, por ejemplo UNMSM, UNI, PUCP o ULima") @RequestParam(required = false) String university,
                                                      @Parameter(description = "Año del examen o práctica") @RequestParam(required = false) Integer year,
                                                      @Parameter(description = "Área académica, por ejemplo Química") @RequestParam(required = false) String area,
                                                      @Parameter(description = "Confirmar subida como nueva versión cuando existe duplicado") @RequestParam(required = false, defaultValue = "false") Boolean confirmVersion,
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
    @Operation(summary = "US08 - Buscar documentos con filtros", description = "Busca por universidad, año, área y texto libre combinable contra título y metadatos.")
    @ApiResponse(responseCode = "200", description = "Resultado de búsqueda, con mensaje de no encontrados cuando la lista está vacía", content = @Content(schema = @Schema(implementation = DocumentSearchResponse.class)))
    public ResponseEntity<DocumentSearchResponse> search(@RequestParam(required = false) String university,
                                                         @RequestParam(required = false) Integer year,
                                                         @RequestParam(required = false) String area,
                                                         @RequestParam(required = false, name = "q") String query) {
        return ResponseEntity.ok(documentService.search(university, year, area, query));
    }

    @GetMapping("/{id}/viewer")
    @Operation(summary = "US09 - Obtener configuración del visor PDF", description = "Devuelve URLs y capacidades para que el frontend abra un visor mobile-first con scroll, zoom, rotación y carga bajo demanda.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuración del visor", content = @Content(schema = @Schema(implementation = DocumentViewerResponse.class))),
            @ApiResponse(responseCode = "422", description = "No se puede previsualizar el archivo")
    })
    public ResponseEntity<DocumentViewerResponse> viewer(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getViewer(id));
    }

    @GetMapping("/{id}/preview")
    @Operation(summary = "US09 - Previsualizar PDF inline", description = "Sirve el PDF con Content-Disposition inline y soporte para Range requests, útil para cargar páginas bajo demanda.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF completo"),
            @ApiResponse(responseCode = "206", description = "Fragmento del PDF para carga parcial"),
            @ApiResponse(responseCode = "422", description = "Archivo dañado o no previsualizable")
    })
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
    @Operation(summary = "US07 - Descargar documento", description = "Aplica límite diario de 5 descargas a usuarios gratuitos. Usuarios premium o descargas con monedas no consumen el límite diario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Descarga iniciada", content = @Content(schema = @Schema(implementation = DownloadDocumentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Saldo insuficiente de monedas"),
            @ApiResponse(responseCode = "429", description = "Límite diario alcanzado")
    })
    public ResponseEntity<DownloadDocumentResponse> download(@PathVariable Long id,
                                                             @RequestParam(defaultValue = "false") boolean useCoins,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        DownloadDocumentResponse document = documentService.download(id, userDetails.getUsername(), useCoins);
        return ResponseEntity.ok(document);
    }

    @PatchMapping("/{id}/anonymous")
    @Operation(summary = "Alternar anonimato del documento", description = "Permite al autor ocultar o mostrar su nombre en el documento.")
    @ApiResponse(responseCode = "200", description = "Anonimato actualizado", content = @Content(schema = @Schema(implementation = DocumentResponse.class)))
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

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        HttpStatus status = ex.getMessage() != null && ex.getMessage().toLowerCase().contains("no encontrado")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of("message", ex.getMessage()));
    }
}
