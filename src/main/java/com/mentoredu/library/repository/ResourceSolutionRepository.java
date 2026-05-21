package com.mentoredu.library.repository;

import com.mentoredu.library.model.ResourceSolution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResourceSolutionRepository extends JpaRepository<ResourceSolution, UUID> {

    boolean existsByResourceIdAndStudentId(UUID resourceId, UUID studentId);

    Optional<ResourceSolution> findByResourceIdAndStudentId(UUID resourceId, UUID studentId);

    List<ResourceSolution> findAllByResourceId(UUID resourceId);
}
