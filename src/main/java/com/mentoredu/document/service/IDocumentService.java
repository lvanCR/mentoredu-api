package com.mentoredu.document.service;

import com.mentoredu.document.dto.DocumentResponse;
import com.mentoredu.document.dto.DownloadDocumentResponse;
import com.mentoredu.document.model.Document;

public interface IDocumentService {
    DocumentResponse publish(Document document, String email);
    DownloadDocumentResponse download(Long documentId, String email, boolean useCoins);
    DocumentResponse toggleAnonymous(Long documentId, String email);
}
