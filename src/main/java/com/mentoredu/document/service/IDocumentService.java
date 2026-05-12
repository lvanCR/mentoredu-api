package com.mentoredu.document.service;

import com.mentoredu.document.dto.DocumentResponse;
import com.mentoredu.document.model.Document;

public interface IDocumentService {
    DocumentResponse publish(Document document, String email);
    DocumentResponse download(Long documentId, String email);
    DocumentResponse toggleAnonymous(Long documentId, String email);
}
