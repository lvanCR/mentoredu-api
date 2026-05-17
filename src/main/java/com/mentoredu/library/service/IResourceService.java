package com.mentoredu.library.service;

import com.mentoredu.library.dto.DownloadResult;
import com.mentoredu.library.dto.PublishResourceRequest;
import com.mentoredu.library.dto.ResourceResponse;

import java.util.List;
import java.util.UUID;

public interface IResourceService {
    ResourceResponse publish(PublishResourceRequest request, String authorEmail);
    List<ResourceResponse> search(String query, String type, String visibility,
                                  UUID institutionId, UUID subjectId, Integer year);
    ResourceResponse getById(UUID resourceId);
    DownloadResult downloadResource(UUID resourceId, String userEmail);
}
