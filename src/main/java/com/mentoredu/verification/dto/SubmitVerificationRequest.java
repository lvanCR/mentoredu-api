package com.mentoredu.verification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitVerificationRequest {
    @NotBlank private String entityType;
}
