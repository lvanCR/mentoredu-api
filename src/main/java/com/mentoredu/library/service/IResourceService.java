package com.mentoredu.library.service;

import com.mentoredu.config.PagedResponse;
import com.mentoredu.library.dto.DownloadResponse;
import com.mentoredu.library.dto.PublishResourceRequest;
import com.mentoredu.library.dto.ResourceResponse;
import com.mentoredu.library.dto.UpdateResourceSettingsRequest;

import java.util.UUID;

public interface IResourceService {
    ResourceResponse publish(PublishResourceRequest request, String authorEmail);
    PagedResponse<ResourceResponse> search(String query, String type, UUID universityId, UUID areaId, UUID careerId, UUID courseId, int page, int size);
    ResourceResponse getById(UUID resourceId, String requesterEmail);
    PagedResponse<ResourceResponse> getByAuthor(String authorEmail, int page, int size);
    DownloadResponse download(UUID resourceId, String userEmail);
    ResourceResponse updateSettings(UUID resourceId, UpdateResourceSettingsRequest request, String requesterEmail);
}
