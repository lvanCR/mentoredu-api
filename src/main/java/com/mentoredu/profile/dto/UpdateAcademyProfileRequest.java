package com.mentoredu.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter @Setter @NoArgsConstructor
public class UpdateAcademyProfileRequest {

    @Size(min = 2, max = 120, message = "El nombre de la academia debe tener entre 2 y 120 caracteres")
    private String academyName;

    @Size(max = 255, message = "La URL del sitio web no puede superar 255 caracteres")
    private String website;

    @Email(message = "El email de contacto no es válido")
    @Size(max = 120, message = "El email de contacto no puede superar 120 caracteres")
    private String contactEmail;
}
