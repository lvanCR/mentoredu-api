package com.mentoredu.profile.repository;

import com.mentoredu.profile.model.AcademyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AcademyProfileRepository extends JpaRepository<AcademyProfile, UUID> {
}
