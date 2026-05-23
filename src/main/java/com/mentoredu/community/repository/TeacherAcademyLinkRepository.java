package com.mentoredu.community.repository;

import com.mentoredu.community.model.TeacherAcademyLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeacherAcademyLinkRepository extends JpaRepository<TeacherAcademyLink, UUID> {
    Optional<TeacherAcademyLink> findByTeacherProfileIdAndAcademyProfileId(UUID teacherProfileId, UUID academyProfileId);
    List<TeacherAcademyLink> findByAcademyProfileId(UUID academyProfileId);
    List<TeacherAcademyLink> findByTeacherProfileId(UUID teacherProfileId);
}
