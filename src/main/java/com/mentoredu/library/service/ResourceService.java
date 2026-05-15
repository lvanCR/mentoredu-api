package com.mentoredu.library.service;

import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.library.dto.PublishResourceRequest;
import com.mentoredu.library.dto.ResourceResponse;
import com.mentoredu.library.model.AcademicResource;
import com.mentoredu.library.repository.AcademicResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceService implements IResourceService {

    private final AcademicResourceRepository resourceRepository;
    private final UserRepository userRepository;

    @Override
    public ResourceResponse publish(PublishResourceRequest request, String authorEmail) {
        var author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        AcademicResource resource = AcademicResource.builder()
                .title(request.getTitle())
                .type(request.getType() != null ? request.getType() : "OTHER")
                .visibility(request.getVisibility() != null ? request.getVisibility() : "PUBLIC")
                .description(request.getDescription())
                .subjectId(request.getSubjectId())
                .institutionId(request.getInstitutionId())
                .year(request.getYear())
                .examCycle(request.getExamCycle())
                .author(author)
                .build();
        return new ResourceResponse(resourceRepository.save(resource));
    }

    @Override
    public List<ResourceResponse> search(String query, String type, String visibility) {
        return resourceRepository.search(
                        clean(query), clean(type), clean(visibility))
                .stream()
                .map(ResourceResponse::new)
                .toList();
    }

    @Override
    public ResourceResponse getById(UUID resourceId) {
        return resourceRepository.findById(resourceId)
                .map(ResourceResponse::new)
                .orElseThrow(() -> new RuntimeException("Recurso no encontrado"));
    }

    private String clean(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
