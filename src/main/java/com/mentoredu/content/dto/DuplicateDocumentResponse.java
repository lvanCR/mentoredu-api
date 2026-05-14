package com.mentoredu.content.dto;

import java.util.List;

public record DuplicateDocumentResponse(
        String message,
        List<DocumentResponse> duplicates
) {}
