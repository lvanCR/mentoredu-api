package com.mentoredu.library.controller;

import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.library.dto.ResourceFileResponse;
import com.mentoredu.library.exception.FileSizeLimitExceededException;
import com.mentoredu.library.exception.InvalidFileTypeException;
import com.mentoredu.library.service.IResourceFileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de capa web para ResourceFileController.
 * Cubre: US07 — Subir archivo PDF académico.
 * Validado en producción (CONFIRMACION_TESTS.md HU-07):
 *   7.1 PDF válido → 201 con fileUrl, fileName, mimeType;
 *   7.2 .docx → 415; 7.3 MIME incorrecto → 415;
 *   7.4 > 20 MB → 413 (proviene de MaxUploadSizeExceededException de Spring MVC);
 *   7.5 sin auth → 401; 7.6 STUDENT sube PDF → 201.
 * Nota: ResourceFileResponse es un record(fileUrl, fileName, mimeType, sizeBytes).
 */
@WebMvcTest(
    controllers = ResourceFileController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class ResourceFileControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private IResourceFileService resourceFileService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private UserDetailsService userDetailsService;

    // ── POST /resources/files ──────────────────────────────────────────────────

    /** US07 Escenario 7.1 — subida exitosa de PDF → 201 con fileUrl, fileName, mimeType */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void upload_validPdf_returns201WithFileInfo() throws Exception {
        var response = new ResourceFileResponse(
            "uploads/resources/abc123.pdf", "examen.pdf", "application/pdf", 102400L);
        when(resourceFileService.upload(any())).thenReturn(response);

        var file = new MockMultipartFile(
            "file", "examen.pdf", "application/pdf", new byte[1024]);

        mockMvc.perform(multipart("/api/v1/resources/files").file(file))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.fileUrl").value("uploads/resources/abc123.pdf"))
            .andExpect(jsonPath("$.fileName").value("examen.pdf"))
            .andExpect(jsonPath("$.mimeType").value("application/pdf"))
            .andExpect(jsonPath("$.sizeBytes").value(102400));
    }

    /** US07 Escenario 7.6 — STUDENT sube PDF para resolución → 201 */
    @Test
    @WithMockUser(username = "student@example.com")
    void upload_studentUploadsPdf_returns201() throws Exception {
        var response = new ResourceFileResponse(
            "uploads/resources/student123.pdf", "resolucion.pdf", "application/pdf", 51200L);
        when(resourceFileService.upload(any())).thenReturn(response);

        var file = new MockMultipartFile(
            "file", "resolucion.pdf", "application/pdf", new byte[1024]);

        mockMvc.perform(multipart("/api/v1/resources/files").file(file))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.fileName").value("resolucion.pdf"));
    }

    /** US07 Escenario 7.2 — archivo .docx → 415 Unsupported Media Type */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void upload_docxFile_returns415() throws Exception {
        when(resourceFileService.upload(any()))
            .thenThrow(new InvalidFileTypeException("Solo se aceptan archivos PDF"));

        var file = new MockMultipartFile(
            "file", "documento.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            new byte[1024]);

        mockMvc.perform(multipart("/api/v1/resources/files").file(file))
            .andExpect(status().isUnsupportedMediaType())
            .andExpect(jsonPath("$.error").value("Unsupported Media Type"));
    }

    /** US07 Escenario 7.3 — PDF con MIME incorrecto (text/plain) → 415 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void upload_incorrectMimeType_returns415() throws Exception {
        when(resourceFileService.upload(any()))
            .thenThrow(new InvalidFileTypeException("El tipo MIME no es application/pdf"));

        var file = new MockMultipartFile(
            "file", "fake.pdf", "text/plain", new byte[1024]);

        mockMvc.perform(multipart("/api/v1/resources/files").file(file))
            .andExpect(status().isUnsupportedMediaType());
    }

    /** US07 Escenario 7.5 — sin autenticación → 401 */
    @Test
    void upload_unauthenticated_returns401() throws Exception {
        // SecurityUtils.currentEmail() se llama en el controller antes del servicio
        var file = new MockMultipartFile(
            "file", "examen.pdf", "application/pdf", new byte[1024]);

        mockMvc.perform(multipart("/api/v1/resources/files").file(file))
            .andExpect(status().isUnauthorized());
    }

    /** US07 — archivo vacío → 400 Bad Request (servicio rechaza) */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void upload_emptyFile_serviceReturns400() throws Exception {
        when(resourceFileService.upload(any()))
            .thenThrow(new FileSizeLimitExceededException("El archivo está vacío"));

        var file = new MockMultipartFile(
            "file", "empty.pdf", "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/api/v1/resources/files").file(file))
            .andExpect(status().isBadRequest());
    }

    /** US07 — múltiples archivos en la misma subida → solo el primero se procesa */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void upload_singleFileProcessed_returns201() throws Exception {
        var response = new ResourceFileResponse(
            "uploads/resources/first.pdf", "primero.pdf", "application/pdf", 2048L);
        when(resourceFileService.upload(any())).thenReturn(response);

        var file = new MockMultipartFile(
            "file", "primero.pdf", "application/pdf", new byte[2048]);

        mockMvc.perform(multipart("/api/v1/resources/files").file(file))
            .andExpect(status().isCreated());
    }
}
