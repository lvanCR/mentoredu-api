package com.mentoredu.document.service;

import com.mentoredu.auth.model.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.document.dto.DocumentResponse;
import com.mentoredu.document.dto.DocumentViewerResponse;
import com.mentoredu.document.exception.PdfPreviewException;
import com.mentoredu.document.model.Document;
import com.mentoredu.document.model.DownloadLog;
import com.mentoredu.document.repository.DocumentRepository;
import com.mentoredu.document.repository.DownloadLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DocumentService implements IDocumentService {

    private static final int DAILY_DOWNLOAD_LIMIT = 5;

    private final DocumentRepository documentRepository;
    private final DownloadLogRepository downloadLogRepository;
    private final UserRepository userRepository;

    @Override
    public DocumentResponse publish(Document document, String email) {
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        document.setAuthor(author);
        return new DocumentResponse(documentRepository.save(document));
    }

    @Override
    public DocumentViewerResponse getViewer(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
        validatePdfPreview(document);

        String previewUrl = "/api/documents/" + document.getId() + "/preview";
        return new DocumentViewerResponse(
                document.getId(),
                document.getTitle(),
                previewUrl,
                "/api/documents/" + document.getId() + "/download",
                previewUrl + "?quality=low",
                true,
                true,
                true,
                true,
                true,
                "RANGE_REQUESTS"
        );
    }

    @Override
    public Path getPreviewPath(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
        validatePdfPreview(document);
        return resolvePreviewPath(document);
    }

    @Override
    @Transactional
    public DocumentResponse download(Long documentId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        if (user.getRole().getName().equals("STUDENT")) {
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            long todayDownloads = downloadLogRepository.countTodayDownloads(user.getId(), startOfDay);
            if (todayDownloads >= DAILY_DOWNLOAD_LIMIT) {
                throw new RuntimeException("Límite diario de " + DAILY_DOWNLOAD_LIMIT + " descargas alcanzado");
            }
        }

        downloadLogRepository.save(DownloadLog.builder()
                .user(user)
                .document(document)
                .build());

        return new DocumentResponse(document);
    }

    @Override
    @Transactional
    public DocumentResponse toggleAnonymous(Long documentId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        if (!document.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para modificar este documento");
        }

        document.setAnonymous(!Boolean.TRUE.equals(document.getAnonymous()));
        return new DocumentResponse(documentRepository.save(document));
    }

    private void validatePdfPreview(Document document) {
        if (!isPdf(document) || !hasValidPdfHeader(resolvePreviewPath(document))) {
            throw new PdfPreviewException();
        }
    }

    private boolean isPdf(Document document) {
        String type = document.getType();
        String fileUrl = document.getFileUrl();
        return (type != null && type.equalsIgnoreCase("PDF"))
                || (fileUrl != null && fileUrl.toLowerCase().endsWith(".pdf"));
    }

    private Path resolvePreviewPath(Document document) {
        String fileUrl = document.getFileUrl();
        if (fileUrl == null || fileUrl.trim().isEmpty() || fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
            throw new PdfPreviewException();
        }

        String localPath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
        Path path = Path.of(localPath).normalize();
        if (!Files.isRegularFile(path)) {
            throw new PdfPreviewException();
        }
        return path;
    }

    private boolean hasValidPdfHeader(Path path) {
        byte[] header = new byte[4];
        try (InputStream inputStream = Files.newInputStream(path)) {
            return inputStream.read(header) == 4
                    && header[0] == '%'
                    && header[1] == 'P'
                    && header[2] == 'D'
                    && header[3] == 'F';
        } catch (IOException ex) {
            return false;
        }
    }
}
