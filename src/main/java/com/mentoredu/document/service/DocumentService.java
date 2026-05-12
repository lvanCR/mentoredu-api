package com.mentoredu.document.service;

import com.mentoredu.auth.model.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.document.dto.DocumentResponse;
import com.mentoredu.document.dto.DownloadDocumentResponse;
import com.mentoredu.document.exception.DailyDownloadLimitExceededException;
import com.mentoredu.document.exception.DuplicateDocumentException;
import com.mentoredu.document.model.Document;
import com.mentoredu.document.model.DownloadLog;
import com.mentoredu.document.repository.DocumentRepository;
import com.mentoredu.document.repository.DownloadLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService implements IDocumentService {

    private static final int DAILY_DOWNLOAD_LIMIT = 5;
    private static final long MAX_PDF_SIZE_BYTES = 10 * 1024 * 1024;
    private static final Path DOCUMENT_STORAGE_PATH = Path.of("uploads", "documents");

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
    @Transactional
    public DocumentResponse uploadPdf(MultipartFile file, String title, String type, String category,
                                      String university, Integer year, String area,
                                      Boolean confirmVersion, String email) {
        validateRequiredUploadFields(file, title, university, year, area);
        validatePdf(file);

        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String fileHash = calculateSha256(file);
        List<Document> duplicates = documentRepository.findDuplicates(
                fileHash,
                title.trim(),
                university.trim(),
                year,
                area.trim()
        );

        if (!duplicates.isEmpty() && !Boolean.TRUE.equals(confirmVersion)) {
            throw new DuplicateDocumentException(duplicates);
        }

        String storedFileName = storeFile(file);
        int nextVersion = duplicates.isEmpty()
                ? 1
                : documentRepository.findMaxVersionForMetadata(title.trim(), university.trim(), year, area.trim()) + 1;

        Document document = Document.builder()
                .title(title.trim())
                .type(hasText(type) ? type.trim() : "PDF")
                .category(hasText(category) ? category.trim() : area.trim())
                .fileUrl("/uploads/documents/" + storedFileName)
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .fileHash(fileHash)
                .version(nextVersion)
                .university(university.trim())
                .year(year)
                .area(area.trim())
                .author(author)
                .build();

        return new DocumentResponse(documentRepository.save(document));
    }

    @Override
    @Transactional
    public DownloadDocumentResponse download(Long documentId, String email, boolean useCoins) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayDownloads = downloadLogRepository.countTodayDownloads(user.getId(), startOfDay);
        boolean unlimitedByRole = hasUnlimitedDownloads(user);
        boolean paidWithCoins = false;
        Integer remainingDailyDownloads = null;

        if (!unlimitedByRole && useCoins) {
            redeemOneCoin(user);
            paidWithCoins = true;
        } else if (!unlimitedByRole) {
            if (todayDownloads >= DAILY_DOWNLOAD_LIMIT) {
                throw new DailyDownloadLimitExceededException();
            }
            remainingDailyDownloads = (int) (DAILY_DOWNLOAD_LIMIT - todayDownloads - 1);
        }

        downloadLogRepository.save(DownloadLog.builder()
                .user(user)
                .document(document)
                .build());

        return new DownloadDocumentResponse(
                "Descarga iniciada",
                new DocumentResponse(document),
                remainingDailyDownloads,
                !unlimitedByRole && !paidWithCoins,
                paidWithCoins
        );
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

    private void validateRequiredUploadFields(MultipartFile file, String title, String university,
                                              Integer year, String area) {
        if (file == null || file.isEmpty() || !hasText(title) || !hasText(university) || year == null || !hasText(area)) {
            throw new IllegalArgumentException("Completa todos los campos obligatorios");
        }
    }

    private void validatePdf(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        boolean hasPdfExtension = originalName != null && originalName.toLowerCase().endsWith(".pdf");
        boolean hasPdfContentType = "application/pdf".equalsIgnoreCase(file.getContentType());

        if (!hasPdfExtension && !hasPdfContentType) {
            throw new IllegalArgumentException("Solo se permiten archivos PDF");
        }

        if (file.getSize() > MAX_PDF_SIZE_BYTES) {
            throw new IllegalArgumentException("El PDF debe pesar menos de 10MB");
        }
    }

    private String calculateSha256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(file.getBytes());
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new RuntimeException("No se pudo procesar el archivo PDF", ex);
        }
    }

    private String storeFile(MultipartFile file) {
        try {
            Files.createDirectories(DOCUMENT_STORAGE_PATH);
            String originalName = file.getOriginalFilename() == null ? "document.pdf" : Path.of(file.getOriginalFilename()).getFileName().toString();
            String storedFileName = UUID.randomUUID() + "-" + originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Files.copy(file.getInputStream(), DOCUMENT_STORAGE_PATH.resolve(storedFileName), StandardCopyOption.REPLACE_EXISTING);
            return storedFileName;
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo guardar el documento", ex);
        }
    }

    private boolean hasUnlimitedDownloads(User user) {
        String roleName = user.getRole().getName();
        return "ADMIN".equals(roleName) || "PREMIUM".equals(roleName);
    }

    private void redeemOneCoin(User user) {
        if (user.getCoins() == null || user.getCoins() < 1) {
            throw new IllegalArgumentException("Saldo insuficiente de monedas");
        }
        user.setCoins(user.getCoins() - 1);
        userRepository.save(user);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
