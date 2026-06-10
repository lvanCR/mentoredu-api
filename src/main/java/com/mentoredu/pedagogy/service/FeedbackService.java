package com.mentoredu.pedagogy.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.service.UserService;
import com.mentoredu.library.model.Resource;
import com.mentoredu.library.repository.ResourceRepository;
import com.mentoredu.library.exception.ResourceNotFoundException;
import com.mentoredu.profile.repository.ProfileRepository;
import com.mentoredu.pedagogy.dto.CreateFeedbackRequest;
import com.mentoredu.pedagogy.dto.FeedbackResponse;
import com.mentoredu.pedagogy.event.FeedbackGivenEvent;
import com.mentoredu.pedagogy.exception.FeedbackAlreadyExistsException;
import com.mentoredu.pedagogy.exception.FeedbackNotFoundException;
import com.mentoredu.pedagogy.exception.SolutionAccessDeniedException;
import com.mentoredu.pedagogy.exception.SolutionNotFoundException;
import com.mentoredu.pedagogy.model.FeedbackEntry;
import com.mentoredu.pedagogy.model.Solution;
import com.mentoredu.pedagogy.model.SolutionStatus;
import com.mentoredu.pedagogy.repository.FeedbackEntryRepository;
import com.mentoredu.pedagogy.repository.SolutionRepository;
import com.mentoredu.pedagogy.service.ResourceAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedbackService implements IFeedbackService {

    private final FeedbackEntryRepository feedbackEntryRepository;
    private final SolutionRepository solutionRepository;
    private final ResourceRepository resourceRepository;
    private final UserService    userService;
    private final ResourceAuthorizationService resourceAuthorizationService;
    private final ApplicationEventPublisher eventPublisher;
    private final ProfileRepository profileRepository;

    @Override
    @Transactional
    public FeedbackResponse create(UUID solutionId, CreateFeedbackRequest request, String authorEmail) {
        Solution solution = solutionRepository.findById(solutionId)
            .orElseThrow(() -> new SolutionNotFoundException("Resolución no encontrada: " + solutionId));
        Resource resource = resourceRepository.findById(solution.getResourceId())
            .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));
        User author = userService.findByEmailOrThrow(authorEmail);
        // RN-10: autorización antes de verificar existencia de feedback (evita exponer 409 a no autorizados)
        if (!resourceAuthorizationService.isAuthorizedForResource(resource, author))
            throw new SolutionAccessDeniedException("Solo el autor del ejercicio puede dar feedback");
        if (feedbackEntryRepository.existsBySolutionId(solutionId))
            throw new FeedbackAlreadyExistsException("Ya existe feedback para esta resolución");
        FeedbackEntry feedback = FeedbackEntry.builder()
            .solution(solution)
            .author(author)
            .score(request.score())
            .body(request.body())
            .build();
        solution.setStatus(SolutionStatus.REVIEWED);
        solutionRepository.save(solution);
        FeedbackEntry saved = feedbackEntryRepository.save(feedback);
        eventPublisher.publishEvent(new FeedbackGivenEvent(
                saved.getId(), solutionId, solution.getStudent().getId(), solution.getResourceId()));
        return enriched(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackResponse getBySolution(UUID solutionId) {
        FeedbackEntry feedback = feedbackEntryRepository.findBySolutionId(solutionId)
            .orElseThrow(() -> new FeedbackNotFoundException("No hay feedback para esta resolución"));
        return enriched(feedback);
    }

    /** Construye FeedbackResponse con el displayName del perfil del autor si existe. */
    @Transactional(readOnly = true)
    public FeedbackResponse enriched(FeedbackEntry f) {
        return profileRepository.findByUserId(f.getAuthor().getId())
            .map(p -> FeedbackResponse.from(f, p.getDisplayName()))
            .orElseGet(() -> FeedbackResponse.from(f));
    }
}
