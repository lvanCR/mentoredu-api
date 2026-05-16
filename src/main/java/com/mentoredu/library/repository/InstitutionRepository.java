package com.mentoredu.library.repository;

import com.mentoredu.library.model.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InstitutionRepository extends JpaRepository<Institution, UUID> {
}
