package com.mentoredu.document.repository;

import com.mentoredu.document.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("""
            SELECT d FROM Document d
            WHERE (:university IS NULL OR LOWER(d.university) = LOWER(:university))
              AND (:year IS NULL OR d.year = :year)
              AND (:area IS NULL OR LOWER(d.area) = LOWER(:area))
              AND (
                    :query IS NULL
                 OR LOWER(d.title) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(d.university) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(d.area) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(d.category) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(d.type) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            ORDER BY d.createdAt DESC
            """)
    List<Document> search(@Param("university") String university,
                          @Param("year") Integer year,
                          @Param("area") String area,
                          @Param("query") String query);
}
