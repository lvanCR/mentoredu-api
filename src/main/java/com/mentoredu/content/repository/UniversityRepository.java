package com.mentoredu.content.repository;

import com.mentoredu.content.model.University;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UniversityRepository extends JpaRepository<University, UUID> {
    Optional<University> findByNameIgnoreCase(String name);
}
