package com.mentoredu.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record VerificationRequestDto(
    @NotBlank @Size(max = 20) String entityType,
    @NotEmpty List<DocEntry> docs
) {
    public record DocEntry(@NotBlank String documentType, @NotBlank String fileUrl) {}
}
