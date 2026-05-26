package com.mentoredu.community.dto;

import com.mentoredu.community.model.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ReviewVerificationRequest {

    @NotNull(message = "La acción es obligatoria (APPROVED o REJECTED)")
    private VerificationStatus action;

    @Size(max = 500, message = "Las notas no pueden superar 500 caracteres")
    private String notes;
}
