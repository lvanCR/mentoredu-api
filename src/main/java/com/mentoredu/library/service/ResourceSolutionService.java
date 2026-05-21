package com.mentoredu.library.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.feedback.model.FeedbackEntry;
import com.mentoredu.feedback.repository.FeedbackRepository;
import com.mentoredu.forum.exception.UserNotFoundException;
import com.mentoredu.gamification.model.enums.PointSourceType;
import com.mentoredu.gamification.service.IGamificationService;
import com.mentoredu.library.dto.MySolutionWithFeedbackResponse;
import com.mentoredu.library.dto.SolutionDetailResponse;
import com.mentoredu.library.dto.SolutionResponse;
import com.mentoredu.library.dto.SubmitSolutionRequest;
import com.mentoredu.library.exception.DuplicateSolutionException;
import com.mentoredu.library.exception.ResourceFileNotFoundException;
import com.mentoredu.library.exception.ResourceNotFoundException;
import com.mentoredu.library.exception.SolutionAccessDeniedException;
import com.mentoredu.library.exception.SolutionNotFoundException;
import com.mentoredu.library.exception.SolutionsNotAllowedException;
import com.mentoredu.library.model.AcademicResource;
import com.mentoredu.library.model.ResourceFile;
import com.mentoredu.library.model.ResourceSolution;
import com.mentoredu.library.repository.AcademicResourceRepository;
import com.mentoredu.library.repository.ResourceFileRepository;
import com.mentoredu.library.repository.ResourceSolutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceSolutionService implements IResourceSolutionService {

    private static final int SOLUTION_SUBMITTED_XP = 3;

    private final ResourceSolutionRepository solutionRepository;
    private final AcademicResourceRepository resourceRepository;
    private final ResourceFileRepository resourceFileRepository;
    private final UserRepository userRepository;
    private final IGamificationService gamificationService;
    private final FeedbackRepository feedbackRepository;

    @Override
    @Transactional
    public SolutionResponse submitSolution(UUID resourceId, SubmitSolutionRequest request, String studentEmail) {
        AcademicResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado: " + resourceId));

        // RN-47: allows_solutions=true requerido para enviar solución
        if (!resource.isAllowsSolutions()) {
            throw new SolutionsNotAllowedException(
                    "Este recurso no acepta resoluciones (RN-47)");
        }

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + studentEmail));

        // RN-45: solo una solución por par (resource_id, student_id)
        if (solutionRepository.existsByResourceIdAndStudentId(resourceId, student.getId())) {
            throw new DuplicateSolutionException(
                    "Ya enviaste una resolución para este recurso (RN-45)");
        }

        resourceFileRepository.findById(request.getFileId())
                .orElseThrow(() -> new ResourceFileNotFoundException(
                        "Archivo no encontrado: " + request.getFileId()));

        ResourceSolution solution = ResourceSolution.builder()
                .resourceId(resourceId)
                .studentId(student.getId())
                .fileId(request.getFileId())
                .status("SUBMITTED")
                .build();

        ResourceSolution saved = solutionRepository.save(solution);

        // RN-31: 3 XP por enviar solución (US30/US39)
        gamificationService.awardPoints(student.getId(), PointSourceType.SOLUTION_SUBMITTED, saved.getId(), SOLUTION_SUBMITTED_XP);

        return new SolutionResponse(saved);
    }

    // -------------------------------------------------------------------------
    // F2.2 — Discovery endpoints (RN-46)
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<SolutionDetailResponse> getSolutionsForResource(UUID resourceId, String requesterEmail) {
        AcademicResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado: " + resourceId));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + requesterEmail));

        String role = requester.getRole().getName();
        List<ResourceSolution> solutions;

        if ("MODERATOR".equals(role) || "ADMIN".equals(role)) {
            // All solutions for the resource
            solutions = solutionRepository.findAllByResourceId(resourceId);
        } else if ("TEACHER".equals(role) || "ACADEMY".equals(role)) {
            // Only if requester is the resource author (RN-46)
            if (!resource.getAuthor().getId().equals(requester.getId())) {
                throw new SolutionAccessDeniedException(
                        "Solo el autor del recurso puede ver sus resoluciones (RN-46)");
            }
            solutions = solutionRepository.findAllByResourceId(resourceId);
        } else {
            // STUDENT: only their own solution
            solutions = solutionRepository.findByResourceIdAndStudentId(resourceId, requester.getId())
                    .map(List::of)
                    .orElse(List.of());
        }

        return solutions.stream()
                .map(s -> {
                    ResourceFile file = resourceFileRepository.findById(s.getFileId()).orElse(null);
                    return new SolutionDetailResponse(s, file);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SolutionDetailResponse getSolutionById(UUID resourceId, UUID solutionId, String requesterEmail) {
        resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado: " + resourceId));

        ResourceSolution solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new SolutionNotFoundException("Solución no encontrada: " + solutionId));

        if (!solution.getResourceId().equals(resourceId)) {
            throw new SolutionNotFoundException("Solución no encontrada: " + solutionId);
        }

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + requesterEmail));

        checkAccessOrThrow(solution, resourceId, requester);

        ResourceFile file = resourceFileRepository.findById(solution.getFileId()).orElse(null);
        return new SolutionDetailResponse(solution, file);
    }

    // -------------------------------------------------------------------------
    // US41 — View my solution and received feedback (RN-46)
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public MySolutionWithFeedbackResponse getMySubmittedSolution(UUID resourceId, String studentEmail) {
        AcademicResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado: " + resourceId));

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + studentEmail));

        ResourceSolution solution = solutionRepository.findByResourceIdAndStudentId(resourceId, student.getId())
                .orElseThrow(() -> new SolutionNotFoundException(
                        "No has enviado ninguna resolución para este recurso"));

        ResourceFile file = resourceFileRepository.findById(solution.getFileId()).orElse(null);

        FeedbackEntry feedback = feedbackRepository.findBySolutionId(solution.getId()).orElse(null);

        return new MySolutionWithFeedbackResponse(solution, resource, file, feedback);
    }

    // RN-46: visible to solution author, resource author, moderators, and admins
    private void checkAccessOrThrow(ResourceSolution solution, UUID resourceId, User requester) {
        String role = requester.getRole().getName();
        if ("MODERATOR".equals(role) || "ADMIN".equals(role)) return;

        if (solution.getStudentId().equals(requester.getId())) return;

        if ("TEACHER".equals(role) || "ACADEMY".equals(role)) {
            AcademicResource resource = resourceRepository.findById(resourceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado: " + resourceId));
            if (resource.getAuthor().getId().equals(requester.getId())) return;
        }

        throw new SolutionAccessDeniedException(
                "No tienes permiso para ver esta resolución (RN-46)");
    }
}
