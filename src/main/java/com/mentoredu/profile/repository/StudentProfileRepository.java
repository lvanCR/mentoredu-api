package com.mentoredu.profile.repository;

import com.mentoredu.profile.model.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {
}
