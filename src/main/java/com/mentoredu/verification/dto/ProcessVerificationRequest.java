package com.mentoredu.verification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ProcessVerificationRequest {

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "VERIFIED|REJECTED", message = "El estado debe ser VERIFIED o REJECTED")
    private String status;
}
