package com.mentoredu.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ReportRequest(
    @NotBlank
    @Pattern(regexp = "THREAD|ANSWER|COMMENT|RESOURCE",
             message = "targetType must be one of: THREAD, ANSWER, COMMENT, RESOURCE")
    @Size(max = 30) String targetType,
    @NotNull UUID targetId,
    @NotBlank @Size(max = 200) String reason
) {}
