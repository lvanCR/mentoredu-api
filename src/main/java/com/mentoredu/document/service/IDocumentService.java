package com.mentoredu.document.service;

import com.mentoredu.document.dto.DocumentResponse;
import com.mentoredu.document.dto.DocumentViewerResponse;
import com.mentoredu.document.model.Document;

import java.nio.file.Path;

public interface IDocumentService {
    DocumentResponse publish(Document document, String email);
    DocumentViewerResponse getViewer(Long documentId);
    Path getPreviewPath(Long documentId);
    DocumentResponse download(Long documentId, String email);
    DocumentResponse toggleAnonymous(Long documentId, String email);
}
