package com.mentoredu.academy.repository;

import com.mentoredu.academy.model.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProgramRepository extends JpaRepository<Program, UUID> {
    List<Program> findByAcademyId(UUID academyId);
    boolean existsByNameAndAcademyId(String name, UUID academyId);
}
