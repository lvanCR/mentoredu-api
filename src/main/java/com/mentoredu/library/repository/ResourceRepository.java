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
        WHERE (:query IS NULL OR unaccent(r.title) ILIKE unaccent(CONCAT('%', :query, '%'))
                              OR unaccent(r.description) ILIKE unaccent(CONCAT('%', :query, '%')))
          AND (:type IS NULL OR r.resource_type = :type)
          AND (:universityId IS NULL OR r.university_id = :universityId)
          AND (:areaId IS NULL OR r.area_id = :areaId)
          AND (:careerId IS NULL OR r.career_id = :careerId)
          AND (:courseId IS NULL OR r.course_id = :courseId)
          AND (:authorId IS NULL OR r.author_user_id = :authorId)
          AND (:resourceYear IS NULL OR r.resource_year = :resourceYear)
        ORDER BY r.created_at DESC
        """,
        countQuery = """
        SELECT COUNT(*) FROM resources r
        WHERE (:query IS NULL OR unaccent(r.title) ILIKE unaccent(CONCAT('%', :query, '%'))
                              OR unaccent(r.description) ILIKE unaccent(CONCAT('%', :query, '%')))
          AND (:type IS NULL OR r.resource_type = :type)
          AND (:universityId IS NULL OR r.university_id = :universityId)
          AND (:areaId IS NULL OR r.area_id = :areaId)
          AND (:careerId IS NULL OR r.career_id = :careerId)
          AND (:courseId IS NULL OR r.course_id = :courseId)
          AND (:authorId IS NULL OR r.author_user_id = :authorId)
          AND (:resourceYear IS NULL OR r.resource_year = :resourceYear)
        """,
        nativeQuery = true)
    Page<Resource> search(
        @Param("query")        String query,
        @Param("type")         String type,
        @Param("universityId") UUID   universityId,
        @Param("areaId")       UUID   areaId,
        @Param("careerId")     UUID   careerId,
        @Param("courseId")     UUID   courseId,
        @Param("authorId")     UUID   authorId,
        @Param("resourceYear") Integer resourceYear,
        Pageable pageable
    );
    @Query(value = """
        SELECT DISTINCT r.* FROM resources r
        LEFT JOIN universities u ON u.id = r.university_id
        LEFT JOIN areas a ON a.id = r.area_id
        LEFT JOIN careers ca ON ca.id = r.career_id
        LEFT JOIN courses c ON c.id = r.course_id
        WHERE (:query IS NULL OR unaccent(r.title) ILIKE unaccent(CONCAT('%', :query, '%'))
                              OR unaccent(r.description) ILIKE unaccent(CONCAT('%', :query, '%'))
                              OR unaccent(u.name) ILIKE unaccent(CONCAT('%', :query, '%'))
                              OR unaccent(a.name) ILIKE unaccent(CONCAT('%', :query, '%'))
                              OR unaccent(ca.name) ILIKE unaccent(CONCAT('%', :query, '%'))
                              OR unaccent(c.name) ILIKE unaccent(CONCAT('%', :query, '%'))
                              OR unaccent(c.description) ILIKE unaccent(CONCAT('%', :query, '%')))
          AND (:type IS NULL OR r.resource_type = :type)
        ORDER BY r.created_at DESC
        """,
        nativeQuery = true)
    Page<Resource> assistantSearch(
        @Param("query") String query,
        @Param("type") String type,
        Pageable pageable
    );
}