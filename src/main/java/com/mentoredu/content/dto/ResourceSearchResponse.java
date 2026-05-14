package com.mentoredu.content.dto;

import java.util.List;

public record ResourceSearchResponse(
        String message,
        List<ResourceResponse> resources
) {}
