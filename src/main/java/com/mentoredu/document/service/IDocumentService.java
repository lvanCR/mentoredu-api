package com.mentoredu.document.service;

import com.mentoredu.document.model.Document;

public interface IDocumentService {
    Document publish(Document document, String email);
    Document download(Long documentId, String email);
}
