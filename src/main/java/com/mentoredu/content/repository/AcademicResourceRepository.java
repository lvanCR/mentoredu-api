package com.mentoredu.content.repository;

import com.mentoredu.content.model.AcademicResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AcademicResourceRepository extends JpaRepository<AcademicResource, UUID> {

    @Query("""
            SELECT r FROM AcademicResource r
            JOIN FETCH r.author
            WHERE (:fileHash IS NOT NULL AND r.fileHash = :fileHash)
               OR (
                    LOWER(r.title) = LOWER(:title)
                AND LOWER(r.university) = LOWER(:university)
                AND r.year = :year
                AND LOWER(r.area) = LOWER(:area)
               )
            """)
    List<AcademicResource> findDuplicates(@Param("fileHash") String fileHash,
                                          @Param("title") String title,
                                          @Param("university") String university,
                                          @Param("year") Integer year,
                                          @Param("area") String area);

    @Query("""
            SELECT COALESCE(MAX(r.version), 0) FROM AcademicResource r
            WHERE LOWER(r.title) = LOWER(:title)
              AND LOWER(r.university) = LOWER(:university)
              AND r.year = :year
              AND LOWER(r.area) = LOWER(:area)
            """)
    Integer findMaxVersionForMetadata(@Param("title") String title,
                                      @Param("university") String university,
                                      @Param("year") Integer year,
                                      @Param("area") String area);

    @Query("""
            SELECT r FROM AcademicResource r
            JOIN FETCH r.author
            WHERE (:university IS NULL OR LOWER(r.university) = LOWER(:university))
              AND (:year IS NULL OR r.year = :year)
              AND (:area IS NULL OR LOWER(r.area) = LOWER(:area))
              AND (
                    :query IS NULL
                 OR LOWER(r.title) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(r.university) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(r.area) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(r.category) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(r.type) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            ORDER BY r.createdAt DESC
            """)
    List<AcademicResource> search(@Param("university") String university,
                                  @Param("year") Integer year,
                                  @Param("area") String area,
                                  @Param("query") String query);
}
