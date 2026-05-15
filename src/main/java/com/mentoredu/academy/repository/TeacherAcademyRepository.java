package com.mentoredu.academy.repository;

import com.mentoredu.academy.model.TeacherAcademy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TeacherAcademyRepository extends JpaRepository<TeacherAcademy, TeacherAcademy.TeacherAcademyId> {
    List<TeacherAcademy> findByIdAcademyId(UUID academyId);
    List<TeacherAcademy> findByIdTeacherProfileId(UUID teacherProfileId);
}
