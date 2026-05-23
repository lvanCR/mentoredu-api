package com.mentoredu.community.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAssociationRequest(
    @NotNull(message = "El ID del perfil de academia es obligatorio") UUID academyProfileId
) {}
