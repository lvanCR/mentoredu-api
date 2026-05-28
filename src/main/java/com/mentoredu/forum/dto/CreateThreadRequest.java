package com.mentoredu.forum.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateThreadRequest {

    @NotBlank(message = "title is required")
    @Size(max = 160, message = "title must not exceed 160 characters")
    private String title;

    @NotBlank(message = "body is required")
    private String body;

    @JsonProperty("isAnonymous")
    private boolean anonymous = false;

    // Clasificación multi-modal — al menos uno de los tres es obligatorio (RN-12).
    // Validación semántica en ThreadService.validateClassification().

    /** Modo Institucional. Requerido si areaId está presente. */
    private UUID universityId;

    /** Modo Institucional con bloque. Solo válido con universityId. */
    private UUID areaId;

    /** Modo Académico global. No puede coexistir con careerId. */
    private UUID courseId;

    /** Modo Vocacional. No puede coexistir con courseId. */
    private UUID careerId;
}
