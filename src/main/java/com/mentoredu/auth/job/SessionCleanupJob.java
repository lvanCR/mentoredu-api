package com.mentoredu.auth.job;

import com.mentoredu.auth.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionCleanupJob {

    private final SessionRepository sessionRepository;

    // Runs daily at 03:00 UTC
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpiredAndRevokedSessions() {
        LocalDateTime expiredBefore = LocalDateTime.now();
        LocalDateTime revokedBefore = LocalDateTime.now().minusDays(30);
        int deleted = sessionRepository.deleteExpiredAndRevoked(expiredBefore, revokedBefore);
        log.info("Session cleanup: deleted {} expired/revoked sessions", deleted);
    }
}
