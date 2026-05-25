package com.mentoredu.auth.repository;

import com.mentoredu.auth.entity.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByRefreshToken(String refreshToken);

    @Modifying
    @Query("UPDATE Session s SET s.revokedAt = :revokedAt WHERE s.user.id = :userId AND s.revokedAt IS NULL")
    int revokeAllActiveByUserId(@Param("userId") UUID userId, @Param("revokedAt") LocalDateTime revokedAt);

    @Query("SELECT s FROM Session s WHERE s.user.id = :userId AND s.revokedAt IS NULL AND s.expiresAt > :now ORDER BY s.createdAt ASC")
    Page<Session> findActiveByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Session s WHERE s.expiresAt < :expiredBefore OR (s.revokedAt IS NOT NULL AND s.revokedAt < :revokedBefore)")
    int deleteExpiredAndRevoked(@Param("expiredBefore") LocalDateTime expiredBefore,
                                @Param("revokedBefore") LocalDateTime revokedBefore);
}
