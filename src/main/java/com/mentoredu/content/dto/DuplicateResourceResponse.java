package com.mentoredu.content.dto;

import java.util.List;

public record DuplicateResourceResponse(
        String message,
        List<ResourceResponse> duplicates
) {}
