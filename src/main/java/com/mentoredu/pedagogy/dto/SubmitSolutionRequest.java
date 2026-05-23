package com.mentoredu.pedagogy.dto;

import jakarta.validation.constraints.Size;

public record SubmitSolutionRequest(
    @Size(max = 500) String fileUrl,
    @Size(max = 10000) String content
) {}
