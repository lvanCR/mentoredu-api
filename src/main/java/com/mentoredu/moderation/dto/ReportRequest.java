package com.mentoredu.moderation.dto;

import com.mentoredu.moderation.model.enums.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class ReportRequest {

    @NotNull
    private TargetType targetType;

    @NotNull
    private UUID targetId;

    @NotBlank
    private String reason;
}
