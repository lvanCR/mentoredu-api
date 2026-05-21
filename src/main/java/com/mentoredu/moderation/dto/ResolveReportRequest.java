package com.mentoredu.moderation.dto;

import com.mentoredu.moderation.model.enums.ModerationActionType;
import com.mentoredu.moderation.model.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResolveReportRequest {

    @NotNull
    private ReportStatus resolution;

    @NotNull
    private ModerationActionType actionType;

    private String notes;
}
