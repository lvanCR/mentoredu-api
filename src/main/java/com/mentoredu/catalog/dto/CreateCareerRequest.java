package com.mentoredu.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCareerRequest(
    @NotNull UUID areaId,
    @NotBlank @Size(max = 120) String name,
    String description
) {}
