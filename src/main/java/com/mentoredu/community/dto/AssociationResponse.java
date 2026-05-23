package com.mentoredu.community.dto;

import com.mentoredu.community.model.TeacherAcademyLink;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class AssociationResponse {

    private final UUID id;
    private final UUID teacherProfileId;
    private final UUID academyProfileId;
    private final String status;
    private final LocalDateTime requestedAt;
    private final LocalDateTime resolvedAt;

    public AssociationResponse(TeacherAcademyLink link) {
        this.id               = link.getId();
        this.teacherProfileId = link.getTeacherProfileId();
        this.academyProfileId = link.getAcademyProfileId();
        this.status           = link.getStatus();
        this.requestedAt      = link.getRequestedAt();
        this.resolvedAt       = link.getResolvedAt();
    }
}
