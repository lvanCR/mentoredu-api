package com.mentoredu.profile.repository;

import com.mentoredu.profile.model.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, UUID> {
}
