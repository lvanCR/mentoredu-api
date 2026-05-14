package com.mentoredu.content.service;

import com.mentoredu.content.dto.DownloadResourceResponse;
import com.mentoredu.content.dto.PublishResourceRequest;
import com.mentoredu.content.dto.ResourceResponse;
import com.mentoredu.content.dto.ResourceSearchResponse;
import com.mentoredu.content.dto.ResourceViewerResponse;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

public interface IResourceService {
    ResourceResponse publish(PublishResourceRequest request, String authorEmail);

    ResourceResponse uploadPdf(MultipartFile file, String title, String type, String category,
                               String university, Integer year, String area,
                               Boolean confirmVersion, String email);

    ResourceSearchResponse search(String university, Integer year, String area, String query);

    ResourceViewerResponse getViewer(UUID resourceId);

    Path getPreviewPath(UUID resourceId);

    DownloadResourceResponse download(UUID resourceId, String email, boolean useCoins);

    ResourceResponse toggleAnonymous(UUID resourceId, String email);
}
