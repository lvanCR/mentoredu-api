package com.mentoredu.pedagogy.repository;

import com.mentoredu.pedagogy.model.Solution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SolutionRepository extends JpaRepository<Solution, UUID> {
    Optional<Solution> findByResourceIdAndStudentId(UUID resourceId, UUID studentId);
    boolean existsByResourceIdAndStudentId(UUID resourceId, UUID studentId);
    List<Solution> findByResourceId(UUID resourceId);
    List<Solution> findByStudentId(UUID studentId);
    List<Solution> findByStudentIdAndResourceIdIn(UUID studentId, Collection<UUID> resourceIds);

    @Query("SELECT s FROM Solution s WHERE s.student.id = :studentId ORDER BY s.submittedAt DESC")
    Page<Solution> findByStudentIdPaged(@Param("studentId") UUID studentId, Pageable pageable);

    @Query("SELECT s FROM Solution s ORDER BY s.submittedAt DESC")
    Page<Solution> findAllReceived(Pageable pageable);

    @Query("""
        SELECT s FROM Solution s
        WHERE s.resourceId IN (
            SELECT r.id FROM Resource r WHERE r.author.id IN :authorIds
        )
        ORDER BY s.submittedAt DESC
        """)
    Page<Solution> findReceivedByAuthorIds(@Param("authorIds") Collection<UUID> authorIds, Pageable pageable);
}
