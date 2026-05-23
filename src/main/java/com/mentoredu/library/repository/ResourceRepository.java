package com.mentoredu.library.repository;

import com.mentoredu.library.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    List<Resource> findByAuthorId(UUID authorId);

    @Query("""
        SELECT r FROM Resource r
        WHERE (:query IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :query, '%')))
          AND (:type IS NULL OR r.resourceType = :type)
          AND (:universityId IS NULL OR r.universityId = :universityId)
          AND (:areaId IS NULL OR r.areaId = :areaId)
          AND (:careerId IS NULL OR r.careerId = :careerId)
          AND (:courseId IS NULL OR r.courseId = :courseId)
          AND r.visibility != 'PRIVATE'
        ORDER BY r.createdAt DESC
        """)
    List<Resource> search(
        @Param("query")        String query,
        @Param("type")         String type,
        @Param("universityId") UUID   universityId,
        @Param("areaId")       UUID   areaId,
        @Param("careerId")     UUID   careerId,
        @Param("courseId")     UUID   courseId
    );
}
