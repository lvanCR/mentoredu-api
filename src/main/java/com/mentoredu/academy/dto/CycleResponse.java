package com.mentoredu.academy.dto;

import com.mentoredu.academy.model.Cycle;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class CycleResponse {

    private final UUID          id;
    private final UUID          academyId;
    private final String        name;
    private final String        cycleType;
    private final LocalDate     startDate;
    private final LocalDate     endDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CycleResponse(Cycle cycle) {
        this.id        = cycle.getId();
        this.academyId = cycle.getAcademyId();
        this.name      = cycle.getName();
        this.cycleType = cycle.getCycleType();
        this.startDate = cycle.getStartDate();
        this.endDate   = cycle.getEndDate();
        this.createdAt = cycle.getCreatedAt();
        this.updatedAt = cycle.getUpdatedAt();
    }
}
