package com.mentoredu.document.service;

import com.mentoredu.auth.model.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.document.dto.DocumentResponse;
import com.mentoredu.document.dto.DocumentSearchResponse;
import com.mentoredu.document.model.Document;
import com.mentoredu.document.model.DownloadLog;
import com.mentoredu.document.repository.DocumentRepository;
import com.mentoredu.document.repository.DownloadLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    public DocumentSearchResponse search(String university, Integer year, String area, String query) {
        List<DocumentResponse> documents = documentRepository.search(
                        clean(university),
                        year,
                        clean(area),
                        clean(query)
                )
                .stream()
                .map(DocumentResponse::new)
                .toList();

        String message = documents.isEmpty()
                ? "No se encontraron documentos con esos criterios. Prueba con otros filtros."
                : "Documentos encontrados";

        return new DocumentSearchResponse(message, documents);
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

    private String clean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
