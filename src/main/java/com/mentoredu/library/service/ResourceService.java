package com.mentoredu.library.service;

import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.library.dto.PublishResourceRequest;
import com.mentoredu.library.dto.ResourceResponse;
import com.mentoredu.library.exception.DuplicateResourceException;
import com.mentoredu.library.exception.ResourceFileNotFoundException;
import com.mentoredu.library.model.AcademicResource;
import com.mentoredu.library.repository.AcademicResourceRepository;
import com.mentoredu.library.repository.ResourceFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceService implements IResourceService {

    private final AcademicResourceRepository resourceRepository;
    private final ResourceFileRepository resourceFileRepository;
    private final UserRepository userRepository;

    @Override
    public ResourceResponse publish(PublishResourceRequest request, String authorEmail) {
        var author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + authorEmail));

        resourceFileRepository.findById(request.getFileId())
                .orElseThrow(() -> new ResourceFileNotFoundException(
                        "Resource file not found: " + request.getFileId()));

        if (resourceRepository.existsByFileId(request.getFileId())) {
            throw new DuplicateResourceException(
                    "A resource already exists for file: " + request.getFileId());
        }

        String visibility = (request.getVisibility() != null && !request.getVisibility().isBlank())
                ? request.getVisibility() : "PUBLIC";

        AcademicResource resource = AcademicResource.builder()
                .title(request.getTitle())
                .type(request.getType())
                .visibility(visibility)
                .description(request.getDescription())
                .subjectId(request.getSubjectId())
                .institutionId(request.getInstitutionId())
                .fileId(request.getFileId())
                .year(request.getYear())
                .examCycle(request.getExamCycle())
                .author(author)
                .build();

        return new ResourceResponse(resourceRepository.save(resource));
    }

    @Override
    public List<ResourceResponse> search(String query, String type, String visibility) {
        return resourceRepository.search(clean(query), clean(type), clean(visibility))
                .stream()
                .map(ResourceResponse::new)
                .toList();
    }

    @Override
    public ResourceResponse getById(UUID resourceId) {
        return resourceRepository.findById(resourceId)
                .map(ResourceResponse::new)
                .orElseThrow(() -> new RuntimeException("Resource not found: " + resourceId));
    }

    private String clean(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
