package com.mentoredu.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ReviewVerificationRequest {

    @NotBlank(message = "La acción es obligatoria")
    @Pattern(regexp = "APPROVED|REJECTED", message = "La acción debe ser APPROVED o REJECTED")
    private String action;

    @Size(max = 500, message = "Las notas no pueden superar 500 caracteres")
    private String notes;
}
