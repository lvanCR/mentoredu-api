package com.mentoredu.pedagogy.repository;

import com.mentoredu.pedagogy.model.Solution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SolutionRepository extends JpaRepository<Solution, UUID> {
    Optional<Solution> findByResourceIdAndStudentId(UUID resourceId, UUID studentId);
    boolean existsByResourceIdAndStudentId(UUID resourceId, UUID studentId);
    List<Solution> findByResourceId(UUID resourceId);
    List<Solution> findByStudentId(UUID studentId);
}
