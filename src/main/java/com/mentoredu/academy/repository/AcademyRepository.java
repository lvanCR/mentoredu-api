package com.mentoredu.academy.repository;

import com.mentoredu.academy.model.Academy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AcademyRepository extends JpaRepository<Academy, UUID> {
    List<Academy> findByOwnerProfileId(UUID ownerProfileId);
}
