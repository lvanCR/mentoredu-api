package com.mentoredu.forum.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.service.UserService;
import com.mentoredu.catalog.service.ICatalogService;
import com.mentoredu.config.PagedResponse;
import com.mentoredu.forum.dto.CreateThreadRequest;
import com.mentoredu.forum.dto.ThreadResponse;
import com.mentoredu.forum.exception.ThreadClosedException;
import com.mentoredu.forum.exception.ThreadNotFoundException;
import com.mentoredu.forum.exception.ThreadNotOwnedException;
import com.mentoredu.forum.model.ForumThread;
import com.mentoredu.forum.model.ThreadStatus;
import com.mentoredu.forum.repository.ReactionRepository;
import com.mentoredu.forum.repository.ThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ThreadService implements IThreadService {

    private static final String THREAD = "THREAD";

    private final ThreadRepository  threadRepository;
    private final ReactionRepository reactionRepository;
    private final UserService        userService;
    private final ICatalogService    catalogService;

    // -------------------------------------------------------------------------
    // US12 — Create forum thread
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ThreadResponse create(CreateThreadRequest request, String authorEmail) {
        User user = userService.findByEmailOrThrow(authorEmail);

        validateClassification(request);

        ForumThread thread = ForumThread.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .anonymous(request.isAnonymous())
                .status(ThreadStatus.OPEN)
                .author(user)
                .universityId(request.getUniversityId())
                .areaId(request.getAreaId())
                .courseId(request.getCourseId())
                .careerId(request.getCareerId())
                .build();

        return toResponse(threadRepository.save(thread), user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ThreadResponse> listRecent(int page, int size, String currentUserEmail, UUID authorId) {
        UUID userId = userService.findByEmailOrThrow(currentUserEmail).getId();
        // Own profile → include anonymous threads; third party → hide them (privacy)
        boolean includeAnonymous = authorId == null || authorId.equals(userId);
        return PagedResponse.from(
                threadRepository.findByAuthorIdOrAll(authorId, includeAnonymous, PagedResponse.toPageRequest(page, size)),
                t -> toResponse(t, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public ThreadResponse get(UUID id, String currentUserEmail) {
        UUID userId = userService.findByEmailOrThrow(currentUserEmail).getId();
        return threadRepository.findById(id)
                .map(t -> toResponse(t, userId))
                .orElseThrow(() -> new ThreadNotFoundException("Hilo no encontrado: " + id));
    }

    // -------------------------------------------------------------------------
    // US13 — Close forum thread (RN-14: solo autor o MODERATOR/ADMIN)
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ThreadResponse close(UUID id, String requesterEmail) {
        ForumThread thread = threadRepository.findById(id)
                .orElseThrow(() -> new ThreadNotFoundException("Hilo no encontrado: " + id));

        if (thread.getStatus() == ThreadStatus.CLOSED) {
            throw new ThreadClosedException("El hilo ya está cerrado: " + id);
        }

        User requester = userService.findByEmailOrThrow(requesterEmail);

        boolean isAuthor = thread.getAuthor().getEmail().equals(requesterEmail);
        String role = requester.getRole().getName();
        boolean isModerator = "MODERATOR".equals(role) || "ADMIN".equals(role);

        // RN-14: solo el autor del hilo o un MODERATOR/ADMIN puede cerrarlo
        if (!isAuthor && !isModerator) {
            throw new ThreadNotOwnedException("Solo el autor, un moderador o administrador puede cerrar este hilo: " + id);
        }

        thread.setStatus(ThreadStatus.CLOSED);
        return toResponse(threadRepository.save(thread), requester.getId());
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

        // (1) Área sin universidad está prohibida (más específico que falta de categoría)
        if (areaId != null && uniId == null) {
            throw new IllegalArgumentException(
                    "El área requiere una universidad seleccionada");
        }

        // (2) Al menos una clasificación es obligatoria
        if (uniId == null && courseId == null && careerId == null) {
            throw new IllegalArgumentException(
                    "El hilo requiere al menos una categoría (universityId, courseId o careerId)");
        }

        // (3) Carrera y curso no pueden coexistir
        if (careerId != null && courseId != null) {
            throw new IllegalArgumentException(
                    "No puedes combinar carrera y curso en el mismo hilo");
        }

        // (4) Validar existencia en catálogo
        if (uniId != null && !catalogService.universityExists(uniId)) {
            throw new IllegalArgumentException("Universidad no encontrada: " + uniId);
        }
        if (courseId != null && !catalogService.courseExists(courseId)) {
            throw new IllegalArgumentException("Curso no encontrado: " + courseId);
        }
        if (careerId != null && !catalogService.careerExists(careerId)) {
            throw new IllegalArgumentException("Carrera no encontrada: " + careerId);
        }

        // (5) Área debe pertenecer a la universidad enviada
        if (areaId != null && !catalogService.areaExistsInUniversity(areaId, uniId)) {
            throw new IllegalArgumentException(
                    "El área no pertenece a la universidad seleccionada");
        }

        // (6) Carrera debe pertenecer a la universidad cuando ambas están presentes
        if (careerId != null && uniId != null &&
                !catalogService.careerExistsInUniversity(careerId, uniId)) {
            throw new IllegalArgumentException(
                    "La carrera no pertenece a la universidad seleccionada");
        }
    }

    // -------------------------------------------------------------------------
    // Mapping (RN-13: posts anónimos exponen "Anónimo", autor interno preservado)
    // -------------------------------------------------------------------------

    private ThreadResponse toResponse(ForumThread t, UUID currentUserId) {
        boolean anon   = Boolean.TRUE.equals(t.getAnonymous());
        String display = anon
                ? "Anónimo"
                : t.getAuthor().getFirstName() + " " + t.getAuthor().getLastName();

        int likes    = (int) reactionRepository.countByTargetTypeAndTargetIdAndReactionType(THREAD, t.getId(), "LIKE");
        int dislikes = (int) reactionRepository.countByTargetTypeAndTargetIdAndReactionType(THREAD, t.getId(), "DISLIKE");
        String myReaction = reactionRepository
                .findByUserIdAndTargetTypeAndTargetId(currentUserId, THREAD, t.getId())
                .map(r -> r.getReactionType())
                .orElse(null);

        return ThreadResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .body(t.getBody())
                .anonymous(anon)
                .authorId(anon ? null : t.getAuthor().getId())
                .authorDisplay(display)
                .status(t.getStatus().name())
                .universityId(t.getUniversityId())
                .areaId(t.getAreaId())
                .courseId(t.getCourseId())
                .careerId(t.getCareerId())
                .likeCount(likes)
                .dislikeCount(dislikes)
                .myReaction(myReaction)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
