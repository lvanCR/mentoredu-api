package com.mentoredu.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class AppealRequest {

    @NotNull
    private UUID reportId;

    @NotBlank
    private String reason;
}
