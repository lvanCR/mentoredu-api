package com.mentoredu.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor
public class CreateVerificationRequest {

    @NotBlank(message = "El tipo de entidad es obligatorio")
    @Pattern(regexp = "TEACHER|ACADEMY",
             message = "entityType debe ser TEACHER o ACADEMY")
    @Size(max = 20, message = "El tipo de entidad no puede superar 20 caracteres")
    private String entityType;

    @NotEmpty(message = "Se requiere al menos un documento adjunto (RN-16)")
    private List<DocInput> documents;

    @Getter @Setter @NoArgsConstructor
    public static class DocInput {
        @NotBlank(message = "El tipo de documento es obligatorio")
        @Size(max = 50)
        private String documentType;

        @NotBlank(message = "La URL del documento es obligatoria")
        private String fileUrl;
    }
}
