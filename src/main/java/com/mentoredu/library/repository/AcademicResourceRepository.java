package com.mentoredu.library.repository;

import com.mentoredu.library.model.AcademicResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AcademicResourceRepository extends JpaRepository<AcademicResource, UUID> {

    @Query("""
            SELECT r FROM AcademicResource r
            JOIN FETCH r.author
            WHERE (:query IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:type IS NULL OR r.type = :type)
              AND (:visibility IS NULL OR r.visibility = :visibility)
            ORDER BY r.createdAt DESC
            """)
    List<AcademicResource> search(@Param("query") String query,
                                   @Param("type") String type,
                                   @Param("visibility") String visibility);
}
