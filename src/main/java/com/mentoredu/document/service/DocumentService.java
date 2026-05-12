package com.mentoredu.document.service;

import com.mentoredu.auth.model.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.document.dto.DocumentResponse;
import com.mentoredu.document.dto.DownloadDocumentResponse;
import com.mentoredu.document.exception.DailyDownloadLimitExceededException;
import com.mentoredu.document.model.Document;
import com.mentoredu.document.model.DownloadLog;
import com.mentoredu.document.repository.DocumentRepository;
import com.mentoredu.document.repository.DownloadLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
