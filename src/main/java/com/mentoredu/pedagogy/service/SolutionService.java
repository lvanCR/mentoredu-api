package com.mentoredu.pedagogy.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.library.model.Resource;
import com.mentoredu.library.repository.ResourceRepository;
import com.mentoredu.pedagogy.dto.SolutionResponse;
import com.mentoredu.pedagogy.dto.SubmitSolutionRequest;
import com.mentoredu.pedagogy.exception.DuplicateSolutionException;
import com.mentoredu.pedagogy.exception.SolutionAccessDeniedException;
import com.mentoredu.pedagogy.exception.SolutionNotFoundException;
import com.mentoredu.pedagogy.model.Solution;
import com.mentoredu.pedagogy.model.SolutionStatus;
import com.mentoredu.pedagogy.repository.SolutionRepository;
import com.mentoredu.library.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SolutionService implements ISolutionService {

    private final SolutionRepository solutionRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SolutionResponse submit(UUID resourceId, SubmitSolutionRequest request, String studentEmail) {
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado: " + resourceId));
        if (!resource.isAceptaResoluciones())
            throw new SolutionAccessDeniedException("Este recurso no acepta resoluciones");
        User student = userRepository.findByEmail(studentEmail)
            .orElseThrow(() -> new SolutionAccessDeniedException("Usuario no encontrado"));
        if (solutionRepository.existsByResourceIdAndStudentId(resourceId, student.getId()))
            throw new DuplicateSolutionException("Ya enviaste una resolución para este ejercicio");
        Solution solution = Solution.builder()
            .resourceId(resourceId)
            .student(student)
            .fileUrl(request.fileUrl())
            .content(request.content())
            .status(SolutionStatus.SUBMITTED)
            .build();
        return SolutionResponse.from(solutionRepository.save(solution));
    }

    @Override
    public SolutionResponse getMine(UUID resourceId, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
            .orElseThrow(() -> new SolutionAccessDeniedException("Usuario no encontrado"));
        Solution solution = solutionRepository.findByResourceIdAndStudentId(resourceId, student.getId())
            .orElseThrow(() -> new SolutionNotFoundException("No has enviado resolución para este ejercicio"));
        return SolutionResponse.from(solution);
    }

    @Override
    public List<SolutionResponse> listByResource(UUID resourceId, String requesterEmail) {
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado: " + resourceId));
        User requester = userRepository.findByEmail(requesterEmail)
            .orElseThrow(() -> new SolutionAccessDeniedException("Usuario no encontrado"));
        if (!resource.getAuthor().getId().equals(requester.getId()))
            throw new SolutionAccessDeniedException("Solo el autor del ejercicio puede ver las resoluciones");
        return solutionRepository.findByResourceId(resourceId).stream()
            .map(SolutionResponse::from).toList();
    }
}
