package com.mentoredu.verification.dto;

import com.mentoredu.verification.model.enums.EntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitVerificationRequest {

    @NotNull(message = "entityType is required")
    private EntityType entityType;

    @NotBlank(message = "documentType is required")
    private String documentType;

    @NotBlank(message = "fileUrl is required")
    private String fileUrl;
}
