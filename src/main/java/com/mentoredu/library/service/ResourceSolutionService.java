package com.mentoredu.library.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.forum.exception.UserNotFoundException;
import com.mentoredu.gamification.model.enums.PointSourceType;
import com.mentoredu.gamification.service.IGamificationService;
import com.mentoredu.library.dto.SolutionResponse;
import com.mentoredu.library.dto.SubmitSolutionRequest;
import com.mentoredu.library.exception.DuplicateSolutionException;
import com.mentoredu.library.exception.ResourceFileNotFoundException;
import com.mentoredu.library.exception.ResourceNotFoundException;
import com.mentoredu.library.exception.SolutionsNotAllowedException;
import com.mentoredu.library.model.AcademicResource;
import com.mentoredu.library.model.ResourceSolution;
import com.mentoredu.library.repository.AcademicResourceRepository;
import com.mentoredu.library.repository.ResourceFileRepository;
import com.mentoredu.library.repository.ResourceSolutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
