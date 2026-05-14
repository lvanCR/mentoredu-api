package com.mentoredu.content.dto;

import java.util.List;

public record DocumentSearchResponse(
        String message,
        List<DocumentResponse> documents
) {}
