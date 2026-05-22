package com.mentoredu.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ResolveAppealRequest {

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "ACCEPTED|REJECTED", message = "El estado debe ser ACCEPTED o REJECTED")
    private String status;
}
