package com.mentoredu.library.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.service.UserService;
import com.mentoredu.catalog.service.ICatalogService;
import com.mentoredu.config.PagedResponse;
import com.mentoredu.config.SecurityUtils;
import com.mentoredu.library.dto.DownloadResponse;
import com.mentoredu.library.dto.PublishResourceRequest;
import com.mentoredu.library.dto.ResourceResponse;
import com.mentoredu.library.dto.SubmissionStatusDto;
import com.mentoredu.library.dto.UpdateResourceSettingsRequest;
import com.mentoredu.pedagogy.repository.SolutionRepository;
import com.mentoredu.library.exception.AceptaResolucionesPracticaOnlyException;
import com.mentoredu.library.exception.CareerAreaMismatchException;
import com.mentoredu.library.exception.CourseIdRequiredException;
import com.mentoredu.library.exception.ResourceAccessDeniedException;
import com.mentoredu.library.exception.ResourceNotFoundException;
import com.mentoredu.library.model.DownloadLog;
import com.mentoredu.library.model.Resource;
import com.mentoredu.library.model.ResourceType;
import com.mentoredu.library.repository.DownloadLogRepository;
import com.mentoredu.library.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceService implements IResourceService {

    private final ResourceRepository    resourceRepository;
    private final UserService           userService;
    private final ICatalogService       catalogService;
    private final DownloadLogRepository downloadLogRepository;
    private final SolutionRepository    solutionRepository;

    // -------------------------------------------------------------------------
    // Publish resource (US07+US08)
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ResourceResponse publish(PublishResourceRequest request, String authorEmail) {
        User author = userService.findByEmailOrThrow(authorEmail);

        // RN-05: solo TEACHER, ACADEMY y ADMIN pueden publicar recursos
        String role = author.getRole().getName();
        if (!"TEACHER".equals(role) && !"ACADEMY".equals(role) && !"ADMIN".equals(role)) {
            throw new ResourceAccessDeniedException(
                    "Solo docentes, academias y administradores pueden publicar recursos (RN-05)");
        }

        ResourceType type = request.getResourceType();

        // RN-07: course_id obligatorio salvo para EXAMEN_COMPLETO, GUIA y APUNTES
        if (!type.courseIdOptional() && request.getCourseId() == null) {
            throw new CourseIdRequiredException(
                    "courseId es obligatorio para el tipo " + type);
        }

        // RN-08: acepta_resoluciones solo válido para PRACTICA
        boolean aceptaResoluciones = Boolean.TRUE.equals(request.getAceptaResoluciones());
        if (aceptaResoluciones && type != ResourceType.PRACTICA) {
            throw new AceptaResolucionesPracticaOnlyException(
                    "Solo los recursos de tipo PRACTICA aceptan resoluciones (RN-08)");
        }

        // Validar existencia en catálogo (RN-03)
        if (!catalogService.universityExists(request.getUniversityId())) {
            throw new IllegalArgumentException("Valor no encontrado en catálogo: universityId=" + request.getUniversityId());
        }
        if (!catalogService.areaExists(request.getAreaId())) {
            throw new IllegalArgumentException("Valor no encontrado en catálogo: areaId=" + request.getAreaId());
        }
        if (request.getCourseId() != null && !catalogService.courseExists(request.getCourseId())) {
            throw new IllegalArgumentException("Valor no encontrado en catálogo: courseId=" + request.getCourseId());
        }

        // RN-23: si career_id presente, la carrera debe pertenecer al area_id enviado
        if (request.getCareerId() != null &&
                !catalogService.careerExistsInArea(request.getCareerId(), request.getAreaId())) {
            throw new CareerAreaMismatchException(
                    "La carrera no pertenece al área seleccionada (RN-23)");
        }

        Resource resource = Resource.builder()
                .author(author)
                .title(request.getTitle())
                .universityId(request.getUniversityId())
                .areaId(request.getAreaId())
                .careerId(request.getCareerId())
                .courseId(request.getCourseId())
                .resourceType(type)
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
    @Transactional(readOnly = true)
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
                        PagedResponse.toPageRequest(page, size)),
                ResourceResponse::new);
    }

    // -------------------------------------------------------------------------
    // Get by ID
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public ResourceResponse getById(UUID resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado: " + resourceId));

        SubmissionStatusDto mySubmission = null;
        if (resource.isAceptaResoluciones()) {
            try {
                User student = userService.findByEmailOrThrow(SecurityUtils.currentEmail());
                if ("STUDENT".equals(student.getRole().getName())) {
                    mySubmission = solutionRepository
                            .findByResourceIdAndStudentId(resourceId, student.getId())
                            .map(s -> new SubmissionStatusDto(s.getId(), s.getStatus().name(), s.getSubmittedAt()))
                            .orElse(null);
                }
            } catch (Exception ignored) {
                // No autenticado o rol distinto a STUDENT → mySubmission queda null
            }
        }

        return new ResourceResponse(resource, mySubmission);
    }

    // -------------------------------------------------------------------------
    // Get by author (US11)
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ResourceResponse> getByAuthor(String authorEmail, int page, int size) {
        User author = userService.findByEmailOrThrow(authorEmail);
        return PagedResponse.from(
                resourceRepository.findByAuthorId(author.getId(), PagedResponse.toPageRequest(page, size)),
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

        User requester = userService.findByEmailOrThrow(requesterEmail);

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
            throw new AceptaResolucionesPracticaOnlyException(
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
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado: " + resourceId));

        User user = userService.findByEmailOrThrow(userEmail);

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
