package com.mentoredu.content.service;

import com.mentoredu.auth.model.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.content.dto.DocumentResponse;
import com.mentoredu.content.dto.DocumentSearchResponse;
import com.mentoredu.content.dto.DocumentViewerResponse;
import com.mentoredu.content.dto.DownloadDocumentResponse;
import com.mentoredu.content.dto.PublishDocumentRequest;
import com.mentoredu.content.exception.DailyDownloadLimitExceededException;
import com.mentoredu.content.exception.DuplicateDocumentException;
import com.mentoredu.content.exception.PdfPreviewException;
import com.mentoredu.content.model.Document;
import com.mentoredu.content.model.DownloadLog;
import com.mentoredu.content.repository.DocumentRepository;
import com.mentoredu.content.repository.DownloadLogRepository;
import com.mentoredu.gamification.model.CoinWallet;
import com.mentoredu.gamification.repository.CoinWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
    private final CoinWalletRepository coinWalletRepository;

    @Override
    public DocumentResponse publish(PublishDocumentRequest request, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Document document = Document.builder()
                .title(request.getTitle())
                .type(request.getType())
                .category(request.getCategory())
                .fileUrl(request.getFileUrl())
                .fileName(request.getFileName())
                .university(request.getUniversity() != null ? request.getUniversity() : "")
                .year(request.getYear() != null ? request.getYear() : 0)
                .area(request.getArea() != null ? request.getArea() : "")
                .author(author)
                .build();
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
                fileHash, title.trim(), university.trim(), year, area.trim());

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
    public DocumentSearchResponse search(String university, Integer year, String area, String query) {
        List<DocumentResponse> documents = documentRepository
                .search(clean(university), year, clean(area), clean(query))
                .stream()
                .map(DocumentResponse::new)
                .toList();

        String message = documents.isEmpty()
                ? "No se encontraron documentos con esos criterios. Prueba con otros filtros."
                : "Documentos encontrados";

        return new DocumentSearchResponse(message, documents);
    }

    @Override
    public DocumentViewerResponse getViewer(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
        validatePdfPreview(document);

        String previewUrl = "/api/v1/resources/" + document.getId() + "/preview";
        return new DocumentViewerResponse(
                document.getId(), document.getTitle(),
                previewUrl,
                "/api/v1/resources/" + document.getId() + "/download",
                previewUrl + "?quality=low",
                true, true, true, true, true, "RANGE_REQUESTS"
        );
    }

    @Override
    public Path getPreviewPath(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
        validatePdfPreview(document);
        return resolvePreviewPath(document);
    }

    @Override
    @Transactional
    public DownloadDocumentResponse download(UUID documentId, String email, boolean useCoins) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayDownloads = downloadLogRepository.countTodayDownloads(user.getId(), startOfDay);
        boolean unlimitedByRole = "ADMIN".equals(user.getRole().getName());
        boolean paidWithCoins = false;
        Integer remainingDailyDownloads = null;

        if (!unlimitedByRole && useCoins) {
            redeemOneCoin(user.getId());
            paidWithCoins = true;
        } else if (!unlimitedByRole) {
            if (todayDownloads >= DAILY_DOWNLOAD_LIMIT) {
                throw new DailyDownloadLimitExceededException();
            }
            remainingDailyDownloads = (int) (DAILY_DOWNLOAD_LIMIT - todayDownloads - 1);
        }

        downloadLogRepository.save(DownloadLog.builder().user(user).document(document).build());

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
    public DocumentResponse toggleAnonymous(UUID documentId, String email) {
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
            String originalName = file.getOriginalFilename() == null
                    ? "document.pdf"
                    : Path.of(file.getOriginalFilename()).getFileName().toString();
            String storedFileName = UUID.randomUUID() + "-" + originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Files.copy(file.getInputStream(), DOCUMENT_STORAGE_PATH.resolve(storedFileName),
                    StandardCopyOption.REPLACE_EXISTING);
            return storedFileName;
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo guardar el documento", ex);
        }
    }

    private void validatePdfPreview(Document document) {
        if (!isPdf(document) || !hasValidPdfHeader(resolvePreviewPath(document))) {
            throw new PdfPreviewException();
        }
    }

    private boolean isPdf(Document document) {
        return ("PDF".equalsIgnoreCase(document.getType()))
                || ("application/pdf".equalsIgnoreCase(document.getContentType()))
                || (document.getFileUrl() != null && document.getFileUrl().toLowerCase().endsWith(".pdf"));
    }

    private Path resolvePreviewPath(Document document) {
        String fileUrl = document.getFileUrl();
        if (fileUrl == null || fileUrl.trim().isEmpty()
                || fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
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
        try (InputStream is = Files.newInputStream(path)) {
            return is.read(header) == 4
                    && header[0] == '%' && header[1] == 'P' && header[2] == 'D' && header[3] == 'F';
        } catch (IOException ex) {
            return false;
        }
    }

    private void redeemOneCoin(UUID userId) {
        CoinWallet wallet = coinWalletRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet no encontrada"));
        if (wallet.getBalance() < 1) {
            throw new IllegalArgumentException("Saldo insuficiente de monedas");
        }
        wallet.setBalance(wallet.getBalance() - 1);
        coinWalletRepository.save(wallet);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String clean(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }
}
