package com.mentoredu.library.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.catalog.repository.CareerRepository;
import com.mentoredu.config.PagedResponse;
import com.mentoredu.library.dto.DownloadResponse;
import com.mentoredu.library.dto.PublishResourceRequest;
import com.mentoredu.library.dto.ResourceResponse;
import com.mentoredu.library.dto.UpdateResourceSettingsRequest;
import com.mentoredu.library.exception.ResourceAccessDeniedException;
import com.mentoredu.library.exception.ResourceNotFoundException;
import com.mentoredu.library.model.DownloadLog;
import com.mentoredu.library.model.Resource;
import com.mentoredu.library.model.ResourceType;
import com.mentoredu.library.repository.DownloadLogRepository;
import com.mentoredu.library.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceService implements IResourceService {

    private final ResourceRepository    resourceRepository;
    private final UserRepository        userRepository;
    private final CareerRepository      careerRepository;
    private final DownloadLogRepository downloadLogRepository;

    // -------------------------------------------------------------------------
    // Publish resource (US07+US08)
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ResourceResponse publish(PublishResourceRequest request, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + authorEmail));

        ResourceType type = request.getResourceType();

        // RN-07: course_id obligatorio salvo para EXAMEN_COMPLETO, GUIA y APUNTES
        if (!type.courseIdOptional() && request.getCourseId() == null) {
            throw new IllegalArgumentException(
                    "courseId es obligatorio para el tipo " + type);
        }

        // RN-08: acepta_resoluciones solo válido para PRACTICA
        boolean aceptaResoluciones = Boolean.TRUE.equals(request.getAceptaResoluciones());
        if (aceptaResoluciones && type != ResourceType.PRACTICA) {
            throw new IllegalArgumentException(
                    "Solo los recursos de tipo PRACTICA aceptan resoluciones (RN-08)");
        }

        // RN-05: solo TEACHER, ACADEMY y ADMIN pueden activar aceptaResoluciones
        if (aceptaResoluciones) {
            String role = author.getRole().getName();
            if (!"TEACHER".equals(role) && !"ACADEMY".equals(role) && !"ADMIN".equals(role)) {
                throw new ResourceAccessDeniedException(
                        "Solo docentes, academias y administradores pueden activar aceptaResoluciones (RN-05)");
            }
        }

        // RN-23: si career_id presente, la carrera debe pertenecer al area_id enviado
        if (request.getCareerId() != null &&
                !careerRepository.existsByIdAndAreaId(request.getCareerId(), request.getAreaId())) {
            throw new IllegalArgumentException(
                    "La carrera no pertenece al área seleccionada (RN-23)");
        }

        String visibility = (request.getVisibility() != null && !request.getVisibility().isBlank())
                ? request.getVisibility() : "PUBLIC";

        Resource resource = Resource.builder()
                .author(author)
                .title(request.getTitle())
                .universityId(request.getUniversityId())
                .areaId(request.getAreaId())
                .careerId(request.getCareerId())
                .courseId(request.getCourseId())
                .resourceType(type)
                .visibility(visibility)
                .description(request.getDescription())
                .aceptaResoluciones(aceptaResoluciones)
                .fileUrl(request.getFileUrl())
                .fileName(request.getFileName())
                .mimeType(request.getMimeType())
                .sizeBytes(request.getSizeBytes())
                .build();

        return new ResourceResponse(resourceRepository.save(resource));
    }

    // -------------------------------------------------------------------------
    // Search resources (US09)
    // -------------------------------------------------------------------------

    @Override
    public PagedResponse<ResourceResponse> search(String query, String type, UUID universityId,
                                                   UUID areaId, UUID careerId, UUID courseId,
                                                   int page, int size) {
        ResourceType resourceType = null;
        if (type != null && !type.isBlank()) {
            try {
                resourceType = ResourceType.valueOf(type.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid resourceType: '" + type + "'. Allowed: "
                        + java.util.Arrays.toString(ResourceType.values()));
            }
        }
        String q = (query == null || query.isBlank()) ? null : query.trim();
        String typeStr = (resourceType != null) ? resourceType.name() : null;
        return PagedResponse.from(
                resourceRepository.search(q, typeStr, universityId, areaId, careerId, courseId,
                        PageRequest.of(page, size)),
                ResourceResponse::new);
    }

    // -------------------------------------------------------------------------
    // Get by ID
    // -------------------------------------------------------------------------

    @Override
    public ResourceResponse getById(UUID resourceId) {
        return resourceRepository.findById(resourceId)
                .map(ResourceResponse::new)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + resourceId));
    }

    // -------------------------------------------------------------------------
    // Get by author (US11)
    // -------------------------------------------------------------------------

    @Override
    public PagedResponse<ResourceResponse> getByAuthor(String authorEmail, int page, int size) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + authorEmail));
        return PagedResponse.from(
                resourceRepository.findByAuthorId(author.getId(), PageRequest.of(page, size)),
                ResourceResponse::new);
    }

    // -------------------------------------------------------------------------
    // Update resource settings (US16 Escenario 2)
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ResourceResponse updateSettings(UUID resourceId, UpdateResourceSettingsRequest request, String requesterEmail) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + resourceId));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + requesterEmail));

        if (!resource.getAuthor().getId().equals(requester.getId())) {
            throw new ResourceAccessDeniedException("Solo el autor puede modificar la configuración del recurso");
        }

        boolean activate = Boolean.TRUE.equals(request.getAceptaResoluciones());

        // RN-05: STUDENT no puede activar acepta_resoluciones
        if (activate) {
            String role = requester.getRole().getName();
            if (!"TEACHER".equals(role) && !"ACADEMY".equals(role) && !"ADMIN".equals(role)) {
                throw new ResourceAccessDeniedException(
                        "Solo docentes, academias y administradores pueden activar aceptaResoluciones (RN-05)");
            }
        }

        // RN-08: acepta_resoluciones solo válido para PRACTICA
        if (activate && resource.getResourceType() != ResourceType.PRACTICA) {
            throw new IllegalArgumentException(
                    "Solo los recursos de tipo PRACTICA aceptan resoluciones (RN-08)");
        }

        resource.setAceptaResoluciones(activate);
        return new ResourceResponse(resourceRepository.save(resource));
    }

    // -------------------------------------------------------------------------
    // Download resource (US10)
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public DownloadResponse download(UUID resourceId, String userEmail) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + resourceId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + userEmail));

        downloadLogRepository.save(DownloadLog.builder()
                .user(user)
                .resource(resource)
                .build());

        return DownloadResponse.builder()
                .resourceId(resource.getId())
                .title(resource.getTitle())
                .fileUrl(resource.getFileUrl())
                .fileName(resource.getFileName())
                .mimeType(resource.getMimeType())
                .sizeBytes(resource.getSizeBytes())
                .build();
    }
}
