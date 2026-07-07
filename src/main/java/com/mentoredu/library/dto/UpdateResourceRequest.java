package com.mentoredu.library.dto;

import jakarta.validation.constraints.Size;

public record UpdateResourceRequest(
    @Size(min = 3, max = 160) String title,
    @Size(max = 2000) String description,
    Integer resourceYear,
    Boolean aceptaResoluciones
) {}
