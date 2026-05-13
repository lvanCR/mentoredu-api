package com.mentoredu.document.repository;

import com.mentoredu.document.model.DownloadLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface DownloadLogRepository extends JpaRepository<DownloadLog, java.util.UUID> {

    @Query("SELECT COUNT(d) FROM DownloadLog d WHERE d.user.id = :userId AND d.downloadedAt >= :startOfDay")
    long countTodayDownloads(@Param("userId") java.util.UUID userId, @Param("startOfDay") LocalDateTime startOfDay);
}
