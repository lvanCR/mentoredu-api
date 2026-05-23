package com.mentoredu.catalog.repository;

import com.mentoredu.catalog.model.University;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UniversityRepository extends JpaRepository<University, UUID> {
    boolean existsByName(String name);
    Optional<University> findByName(String name);
}
