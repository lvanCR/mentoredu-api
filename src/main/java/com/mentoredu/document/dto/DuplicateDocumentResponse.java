package com.mentoredu.document.dto;

import java.util.List;

public record DuplicateDocumentResponse(
        String message,
        List<DocumentResponse> duplicates
) {
}
