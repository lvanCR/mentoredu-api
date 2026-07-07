package com.mentoredu.forum.dto;

import jakarta.validation.constraints.Size;

public record UpdateThreadRequest(
    @Size(min = 5, max = 160) String title,
    @Size(min = 10, max = 5000) String body
) {}
