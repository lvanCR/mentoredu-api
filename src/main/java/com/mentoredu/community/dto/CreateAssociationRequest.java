package com.mentoredu.community.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor
public class CreateAssociationRequest {

    @NotNull(message = "El ID del perfil de academia es obligatorio")
    private UUID academyProfileId;
}
