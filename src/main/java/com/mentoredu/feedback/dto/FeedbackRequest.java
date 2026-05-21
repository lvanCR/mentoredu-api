package com.mentoredu.feedback.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
public class FeedbackRequest {

    @NotNull(message = "El ID del estudiante receptor es obligatorio")
    private UUID targetUserId;

    private UUID programId;

    private UUID cycleId;

    /** Opcional: si se proporciona, el feedback queda vinculado a una resolución específica (US40). */
    private UUID solutionId;

    @NotBlank(message = "El cuerpo de la retroalimentación no puede estar vacío")
    private String body;

    @DecimalMin(value = "0.0", message = "La puntuación mínima es 0.0")
    @DecimalMax(value = "20.0", message = "La puntuación máxima es 20.0")
    private BigDecimal score;
}
