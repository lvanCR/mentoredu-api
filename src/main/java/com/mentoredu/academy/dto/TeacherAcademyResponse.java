package com.mentoredu.academy.dto;

import com.mentoredu.academy.model.TeacherAcademy;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class TeacherAcademyResponse {

    private final UUID          teacherProfileId;
    private final UUID          academyId;
    private final LocalDateTime createdAt;

    public TeacherAcademyResponse(TeacherAcademy association) {
        this.teacherProfileId = association.getId().getTeacherProfileId();
        this.academyId        = association.getId().getAcademyId();
        this.createdAt        = association.getCreatedAt();
    }
}
