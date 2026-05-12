package com.mentoredu.document.service;

import com.mentoredu.document.dto.DocumentResponse;
import com.mentoredu.document.dto.DocumentSearchResponse;
import com.mentoredu.document.model.Document;

public interface IDocumentService {
    DocumentResponse publish(Document document, String email);
    DocumentSearchResponse search(String university, Integer year, String area, String query);
    DocumentResponse download(Long documentId, String email);
    DocumentResponse toggleAnonymous(Long documentId, String email);
}
