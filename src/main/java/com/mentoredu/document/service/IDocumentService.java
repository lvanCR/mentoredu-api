package com.mentoredu.document.service;

import com.mentoredu.document.dto.DocumentResponse;
import com.mentoredu.document.dto.DocumentSearchResponse;
import com.mentoredu.document.dto.DocumentViewerResponse;
import com.mentoredu.document.dto.DownloadDocumentResponse;
import com.mentoredu.document.model.Document;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface IDocumentService {
    DocumentResponse publish(Document document, String email);

    DocumentResponse uploadPdf(MultipartFile file, String title, String type, String category,
                               String university, Integer year, String area,
                               Boolean confirmVersion, String email);

    DocumentSearchResponse search(String university, Integer year, String area, String query);

    DocumentViewerResponse getViewer(Long documentId);

    Path getPreviewPath(Long documentId);

    DownloadDocumentResponse download(Long documentId, String email, boolean useCoins);

    DocumentResponse toggleAnonymous(Long documentId, String email);
}
