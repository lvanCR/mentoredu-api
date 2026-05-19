package com.mentoredu.gamification.repository;

import com.mentoredu.gamification.model.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, UUID> {

    @Query("SELECT COALESCE(SUM(pt.pointsDelta), 0) FROM PointTransaction pt WHERE pt.userId = :userId")
    Integer sumPointsByUserId(@Param("userId") UUID userId);

    boolean existsBySourceTypeAndSourceId(String sourceType, UUID sourceId);
}
