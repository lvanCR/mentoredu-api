package com.mentoredu.community.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveReportRequest(@NotBlank String resolutionNote) {}
