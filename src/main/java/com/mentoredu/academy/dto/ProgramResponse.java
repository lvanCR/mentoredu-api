package com.mentoredu.academy.dto;

import com.mentoredu.academy.model.Program;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ProgramResponse {

    private final UUID          id;
    private final UUID          academyId;
    private final String        name;
    private final String        modality;
    private final String        intensity;
    private final String        targetUniversity;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ProgramResponse(Program program) {
        this.id               = program.getId();
        this.academyId        = program.getAcademyId();
        this.name             = program.getName();
        this.modality         = program.getModality();
        this.intensity        = program.getIntensity();
        this.targetUniversity = program.getTargetUniversity();
        this.createdAt        = program.getCreatedAt();
        this.updatedAt        = program.getUpdatedAt();
    }
}
