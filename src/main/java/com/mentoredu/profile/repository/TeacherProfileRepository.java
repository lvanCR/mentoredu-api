package com.mentoredu.profile.repository;

import com.mentoredu.profile.model.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, UUID> {
    Optional<TeacherProfile> findByProfile_UserId(UUID userId);
}
