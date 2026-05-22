package com.mentoredu.forum.service;

import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.catalog.repository.AreaRepository;
import com.mentoredu.catalog.repository.CareerRepository;
import com.mentoredu.catalog.repository.CourseRepository;
import com.mentoredu.catalog.repository.UniversityRepository;
import com.mentoredu.forum.dto.CreateThreadRequest;
import com.mentoredu.forum.dto.ThreadResponse;
import com.mentoredu.forum.exception.ThreadClosedException;
import com.mentoredu.forum.exception.ThreadNotFoundException;
import com.mentoredu.forum.exception.ThreadNotOwnedException;
import com.mentoredu.forum.model.Thread;
import com.mentoredu.forum.repository.ThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ThreadService implements IThreadService {

    private final ThreadRepository   threadRepository;
    private final UserRepository     userRepository;
    private final UniversityRepository universityRepository;
    private final AreaRepository     areaRepository;
    private final CourseRepository   courseRepository;
    private final CareerRepository   careerRepository;

    // -------------------------------------------------------------------------
    // US12 — Create forum thread
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ThreadResponse create(CreateThreadRequest request, String authorEmail) {
        var user = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + authorEmail));

        validateClassification(request);

        Thread thread = Thread.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .anonymous(request.isAnonymous())
                .author(user)
                .universityId(request.getUniversityId())
                .areaId(request.getAreaId())
                .courseId(request.getCourseId())
                .careerId(request.getCareerId())
                .build();

        return toResponse(threadRepository.save(thread));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThreadResponse> listRecent(int page, int size) {
        return threadRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ThreadResponse get(UUID id) {
        return threadRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ThreadNotFoundException("Thread not found: " + id));
    }

    // -------------------------------------------------------------------------
    // US13 — Close forum thread (RN-14: solo autor o MODERATOR/ADMIN)
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ThreadResponse close(UUID id, String requesterEmail) {
        Thread thread = threadRepository.findById(id)
                .orElseThrow(() -> new ThreadNotFoundException("Thread not found: " + id));

        if (!thread.getAuthor().getEmail().equals(requesterEmail)) {
            throw new ThreadNotOwnedException("Only the author can close this thread: " + id);
        }

        if ("CLOSED".equals(thread.getStatus())) {
            throw new ThreadClosedException("Thread is already closed: " + id);
        }

        thread.setStatus("CLOSED");
        return toResponse(threadRepository.save(thread));
    }

    // -------------------------------------------------------------------------
    // RN-12 — Validación de clasificación multi-modal
    // Orden: prohibiciones primero, existencia en catálogo después.
    // -------------------------------------------------------------------------

    private void validateClassification(CreateThreadRequest req) {
        UUID uniId    = req.getUniversityId();
        UUID areaId   = req.getAreaId();
        UUID courseId = req.getCourseId();
        UUID careerId = req.getCareerId();

        // (1) Al menos una clasificación es obligatoria
        if (uniId == null && courseId == null && careerId == null) {
            throw new IllegalArgumentException(
                    "El hilo requiere al menos una categoría (universityId, courseId o careerId)");
        }

        // (2) Área sin universidad está prohibida
        if (areaId != null && uniId == null) {
            throw new IllegalArgumentException(
                    "El área requiere una universidad seleccionada");
        }

        // (3) Carrera y curso no pueden coexistir
        if (careerId != null && courseId != null) {
            throw new IllegalArgumentException(
                    "No puedes combinar carrera y curso en el mismo hilo");
        }

        // (4) Validar existencia en catálogo
        if (uniId != null && !universityRepository.existsById(uniId)) {
            throw new IllegalArgumentException("Universidad no encontrada: " + uniId);
        }
        if (courseId != null && !courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("Curso no encontrado: " + courseId);
        }
        if (careerId != null && !careerRepository.existsById(careerId)) {
            throw new IllegalArgumentException("Carrera no encontrada: " + careerId);
        }

        // (5) Área debe pertenecer a la universidad enviada
        if (areaId != null && !areaRepository.existsByIdAndUniversityId(areaId, uniId)) {
            throw new IllegalArgumentException(
                    "El área no pertenece a la universidad seleccionada");
        }

        // (6) Carrera debe pertenecer a la universidad cuando ambas están presentes
        if (careerId != null && uniId != null &&
                !careerRepository.existsByIdAndUniversityId(careerId, uniId)) {
            throw new IllegalArgumentException(
                    "La carrera no pertenece a la universidad seleccionada");
        }
    }

    // -------------------------------------------------------------------------
    // Mapping (RN-13: posts anónimos exponen "Anónimo", autor interno preservado)
    // -------------------------------------------------------------------------

    private ThreadResponse toResponse(Thread t) {
        String display = Boolean.TRUE.equals(t.getAnonymous())
                ? "Anónimo"
                : t.getAuthor().getFirstName() + " " + t.getAuthor().getLastName();

        return ThreadResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .body(t.getBody())
                .anonymous(Boolean.TRUE.equals(t.getAnonymous()))
                .authorDisplay(display)
                .status(t.getStatus())
                .universityId(t.getUniversityId())
                .areaId(t.getAreaId())
                .courseId(t.getCourseId())
                .careerId(t.getCareerId())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
