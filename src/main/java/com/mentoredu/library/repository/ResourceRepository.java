package com.mentoredu.library.repository;

import com.mentoredu.library.model.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    Page<Resource> findByAuthorId(UUID authorId, Pageable pageable);

    @Query(value = """
        SELECT * FROM resources r
        WHERE (:query IS NULL OR r.title ILIKE CONCAT('%', :query, '%')
                              OR r.description ILIKE CONCAT('%', :query, '%'))
          AND (:type IS NULL OR r.resource_type = :type)
          AND (:universityId IS NULL OR r.university_id = :universityId)
          AND (:areaId IS NULL OR r.area_id = :areaId)
          AND (:careerId IS NULL OR r.career_id = :careerId)
          AND (:courseId IS NULL OR r.course_id = :courseId)
        ORDER BY r.created_at DESC
        """,
        countQuery = """
        SELECT COUNT(*) FROM resources r
        WHERE (:query IS NULL OR r.title ILIKE CONCAT('%', :query, '%')
                              OR r.description ILIKE CONCAT('%', :query, '%'))
          AND (:type IS NULL OR r.resource_type = :type)
          AND (:universityId IS NULL OR r.university_id = :universityId)
          AND (:areaId IS NULL OR r.area_id = :areaId)
          AND (:careerId IS NULL OR r.career_id = :careerId)
          AND (:courseId IS NULL OR r.course_id = :courseId)
        """,
        nativeQuery = true)
    Page<Resource> search(
        @Param("query")        String query,
        @Param("type")         String type,
        @Param("universityId") UUID   universityId,
        @Param("areaId")       UUID   areaId,
        @Param("careerId")     UUID   careerId,
        @Param("courseId")     UUID   courseId,
        Pageable pageable
    );
}
