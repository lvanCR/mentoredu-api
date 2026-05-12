package com.mentoredu.document.service;

import com.mentoredu.document.dto.DocumentResponse;
import com.mentoredu.document.model.Document;
import org.springframework.web.multipart.MultipartFile;

public interface IDocumentService {
    DocumentResponse publish(Document document, String email);
    DocumentResponse uploadPdf(MultipartFile file, String title, String type, String category,
                               String university, Integer year, String area,
                               Boolean confirmVersion, String email);
    DocumentResponse download(Long documentId, String email);
    DocumentResponse toggleAnonymous(Long documentId, String email);
}
