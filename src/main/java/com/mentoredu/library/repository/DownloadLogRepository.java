package com.mentoredu.library.repository;

import com.mentoredu.library.model.DownloadLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DownloadLogRepository extends JpaRepository<DownloadLog, UUID> {

    @Query("SELECT COUNT(d) FROM DownloadLog d WHERE d.user.id = :userId AND d.downloadedAt >= :startOfDay")
    long countTodayDownloads(@Param("userId") UUID userId, @Param("startOfDay") LocalDateTime startOfDay);

    @Modifying
    void deleteByResourceId(UUID resourceId);
}
