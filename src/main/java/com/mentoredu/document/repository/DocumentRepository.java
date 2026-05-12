package com.mentoredu.document.repository;

import com.mentoredu.document.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("""
            SELECT d FROM Document d
            JOIN FETCH d.author
            WHERE (:fileHash IS NOT NULL AND d.fileHash = :fileHash)
               OR (
                    LOWER(d.title) = LOWER(:title)
                AND LOWER(d.university) = LOWER(:university)
                AND d.year = :year
                AND LOWER(d.area) = LOWER(:area)
               )
            """)
    List<Document> findDuplicates(@Param("fileHash") String fileHash,
                                  @Param("title") String title,
                                  @Param("university") String university,
                                  @Param("year") Integer year,
                                  @Param("area") String area);

    @Query("""
            SELECT COALESCE(MAX(d.version), 0) FROM Document d
            WHERE LOWER(d.title) = LOWER(:title)
              AND LOWER(d.university) = LOWER(:university)
              AND d.year = :year
              AND LOWER(d.area) = LOWER(:area)
            """)
    Integer findMaxVersionForMetadata(@Param("title") String title,
                                      @Param("university") String university,
                                      @Param("year") Integer year,
                                      @Param("area") String area);
}
