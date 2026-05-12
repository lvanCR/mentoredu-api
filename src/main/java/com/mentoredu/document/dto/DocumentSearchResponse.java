package com.mentoredu.document.dto;

import java.util.List;

public record DocumentSearchResponse(
        String message,
        List<DocumentResponse> documents
) {
}
