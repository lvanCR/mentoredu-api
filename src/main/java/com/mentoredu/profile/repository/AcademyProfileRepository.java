package com.mentoredu.profile.repository;

import com.mentoredu.profile.model.AcademyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AcademyProfileRepository extends JpaRepository<AcademyProfile, UUID> {
    boolean existsByAcademyName(String academyName);
    boolean existsByRuc(String ruc);
    Optional<AcademyProfile> findByProfile_UserId(UUID userId);
}
