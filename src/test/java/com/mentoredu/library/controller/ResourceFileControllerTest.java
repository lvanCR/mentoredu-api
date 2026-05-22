package com.mentoredu.library.controller;

import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.library.dto.ResourceFileResponse;
import com.mentoredu.library.exception.FileSizeLimitExceededException;
import com.mentoredu.library.exception.InvalidFileTypeException;
import com.mentoredu.library.service.IResourceFileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ResourceFileController.class)
class ResourceFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IResourceFileService resourceFileService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // US07 — Upload PDF resource file
    // =========================================================================

    @Test
    @WithMockUser(username = "user@example.com")
    void upload_withValidPdf_returns201() throws Exception {
        byte[] pdfContent = "%PDF-1.4 test content".getBytes(StandardCharsets.US_ASCII);
        MockMultipartFile file = new MockMultipartFile(
                "file", "examen-uni-2024.pdf", "application/pdf", pdfContent);

        when(resourceFileService.upload(any())).thenReturn(
                new ResourceFileResponse("uploads/resources/examen-uni-2024.pdf",
                        "examen-uni-2024.pdf", "application/pdf", (long) pdfContent.length));

        mockMvc.perform(multipart("/api/v1/resources/files").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("examen-uni-2024.pdf"))
                .andExpect(jsonPath("$.mimeType").value("application/pdf"))
                .andExpect(jsonPath("$.sizeBytes").value(pdfContent.length))
                .andExpect(jsonPath("$.fileUrl").exists());
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void upload_pdfReturnsFileMetadata_returns201() throws Exception {
        byte[] pdfContent = "%PDF-1.7 another valid pdf".getBytes(StandardCharsets.US_ASCII);
        MockMultipartFile file = new MockMultipartFile(
                "file", "guia-algebra.pdf", "application/pdf", pdfContent);

        when(resourceFileService.upload(any())).thenReturn(
                new ResourceFileResponse("uploads/resources/guia-algebra.pdf",
                        "guia-algebra.pdf", "application/pdf", (long) pdfContent.length));

        mockMvc.perform(multipart("/api/v1/resources/files").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileUrl").value("uploads/resources/guia-algebra.pdf"))
                .andExpect(jsonPath("$.fileName").value("guia-algebra.pdf"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void upload_withNonPdfFile_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "document.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "some word content".getBytes());

        when(resourceFileService.upload(any()))
                .thenThrow(new InvalidFileTypeException(
                        "Only PDF files are accepted. Received content type: "
                        + "application/vnd.openxmlformats-officedocument.wordprocessingml.document"));

        mockMvc.perform(multipart("/api/v1/resources/files").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void upload_whenFileTooLarge_returns400() throws Exception {
        byte[] pdfContent = "%PDF-1.4 oversized".getBytes(StandardCharsets.US_ASCII);
        MockMultipartFile file = new MockMultipartFile(
                "file", "large.pdf", "application/pdf", pdfContent);

        when(resourceFileService.upload(any()))
                .thenThrow(new FileSizeLimitExceededException(
                        "File size exceeds the maximum allowed limit of 20 MB."));

        mockMvc.perform(multipart("/api/v1/resources/files").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(
                        "File size exceeds the maximum allowed limit of 20 MB."));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void upload_withCorruptedPdf_returns400() throws Exception {
        byte[] corruptedContent = "CORRUPT_DATA_NOT_PDF".getBytes(StandardCharsets.US_ASCII);
        MockMultipartFile file = new MockMultipartFile(
                "file", "corrupted.pdf", "application/pdf", corruptedContent);

        when(resourceFileService.upload(any()))
                .thenThrow(new InvalidFileTypeException(
                        "The file appears to be corrupted or is not a valid PDF."));

        mockMvc.perform(multipart("/api/v1/resources/files").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(
                        "The file appears to be corrupted or is not a valid PDF."));
    }

    @Test
    void upload_withoutAuth_returns401() throws Exception {
        byte[] pdfContent = "%PDF-1.4 test".getBytes(StandardCharsets.US_ASCII);
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", pdfContent);

        mockMvc.perform(multipart("/api/v1/resources/files")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }
}
