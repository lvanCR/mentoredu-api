package com.mentoredu.content.service;

import com.mentoredu.content.dto.DocumentResponse;
import com.mentoredu.content.dto.DocumentSearchResponse;
import com.mentoredu.content.dto.DocumentViewerResponse;
import com.mentoredu.content.dto.DownloadDocumentResponse;
import com.mentoredu.content.dto.PublishDocumentRequest;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

public interface IDocumentService {
    DocumentResponse publish(PublishDocumentRequest request, String authorEmail);

    DocumentResponse uploadPdf(MultipartFile file, String title, String type, String category,
                               String university, Integer year, String area,
                               Boolean confirmVersion, String email);

    DocumentSearchResponse search(String university, Integer year, String area, String query);

    DocumentViewerResponse getViewer(UUID documentId);

    Path getPreviewPath(UUID documentId);

    DownloadDocumentResponse download(UUID documentId, String email, boolean useCoins);

    DocumentResponse toggleAnonymous(UUID documentId, String email);
}
