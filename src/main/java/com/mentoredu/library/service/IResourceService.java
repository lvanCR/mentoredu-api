package com.mentoredu.library.service;

import com.mentoredu.library.dto.DownloadResponse;
import com.mentoredu.library.dto.PublishResourceRequest;
import com.mentoredu.library.dto.ResourceResponse;

import java.util.List;
import java.util.UUID;

public interface IResourceService {
    ResourceResponse publish(PublishResourceRequest request, String authorEmail);
    List<ResourceResponse> search(String query, String type, UUID universityId, UUID areaId, UUID careerId, UUID courseId);
    ResourceResponse getById(UUID resourceId);
    List<ResourceResponse> getByAuthor(String authorEmail);
    DownloadResponse download(UUID resourceId, String userEmail);
}
