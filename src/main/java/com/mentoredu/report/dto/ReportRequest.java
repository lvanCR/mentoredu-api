package com.mentoredu.report.dto;

import com.mentoredu.report.model.enums.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReportRequest {

    @NotNull
    private TargetType targetType;

    @NotNull
    private Long targetId;

    @NotBlank
    private String reason;
}
