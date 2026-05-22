package com.mentoredu.feedback.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.feedback.dto.FeedbackRequest;
import com.mentoredu.feedback.dto.FeedbackResponse;
import com.mentoredu.feedback.event.FeedbackGivenEvent;
import com.mentoredu.feedback.event.SolutionReviewedEvent;
import com.mentoredu.feedback.exception.FeedbackResourceAuthorshipException;
import com.mentoredu.feedback.exception.FeedbackSolutionNotFoundException;
import com.mentoredu.feedback.exception.FeedbackTargetNotFoundException;
import com.mentoredu.feedback.exception.FeedbackUnauthorizedException;
import com.mentoredu.feedback.model.FeedbackEntry;
import com.mentoredu.feedback.repository.FeedbackRepository;
import com.mentoredu.forum.exception.UserNotFoundException;
import com.mentoredu.gamification.model.enums.PointSourceType;
import com.mentoredu.gamification.service.IGamificationService;
import com.mentoredu.library.model.AcademicResource;
import com.mentoredu.library.model.ResourceSolution;
import com.mentoredu.library.repository.AcademicResourceRepository;
import com.mentoredu.library.repository.ResourceSolutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService implements IFeedbackService {

    private static final int FEEDBACK_GIVEN_XP = 5;
    private static final BigDecimal HIGHLY_RATED_THRESHOLD = new BigDecimal("8.0");

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final ResourceSolutionRepository solutionRepository;
    private final AcademicResourceRepository resourceRepository;
    private final IGamificationService gamificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public FeedbackResponse provideFeedback(String authorEmail, FeedbackRequest request) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + authorEmail));

        String roleName = author.getRole().getName();
        if (!"TEACHER".equals(roleName) && !"ACADEMY".equals(roleName) && !"ADMIN".equals(roleName)) {
            throw new FeedbackUnauthorizedException(
                    "Solo los docentes, academias y administradores pueden emitir retroalimentación académica (RN-36)");
        }

        User target = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new FeedbackTargetNotFoundException(
                        "Estudiante no encontrado: " + request.getTargetUserId()));

        if (!"STUDENT".equals(target.getRole().getName())) {
            throw new FeedbackTargetNotFoundException(
                    "El receptor debe ser un estudiante: " + request.getTargetUserId());
        }

        FeedbackEntry.FeedbackEntryBuilder builder = FeedbackEntry.builder()
                .author(author)
                .target(target)
                .programId(request.getProgramId())
                .cycleId(request.getCycleId())
                .body(request.getBody())
                .score(request.getScore());

        ResourceSolution solution = null;
        AcademicResource resource = null;

        // US40: if solutionId provided, validate RN-48 and mark solution as REVIEWED (RN-49)
        if (request.getSolutionId() != null) {
            solution = solutionRepository.findById(request.getSolutionId())
                    .orElseThrow(() -> new FeedbackSolutionNotFoundException(
                            "Resolución no encontrada: " + request.getSolutionId()));

            resource = resourceRepository.findById(solution.getResourceId())
                    .orElseThrow(() -> new FeedbackSolutionNotFoundException(
                            "Recurso de la resolución no encontrado"));

            // RN-48: only the resource author can give feedback on its solutions
            if (!resource.getAuthor().getId().equals(author.getId())) {
                throw new FeedbackResourceAuthorshipException(
                        "Solo el autor del recurso puede dar feedback a sus resoluciones (RN-48)");
            }

            // RN-49: automatically transition solution status to REVIEWED
            solution.setStatus("REVIEWED");
            solutionRepository.save(solution);

            builder.solutionId(solution.getId()).resourceId(solution.getResourceId());
        }

        FeedbackEntry saved = feedbackRepository.save(builder.build());

        gamificationService.awardPoints(author.getId(), PointSourceType.FEEDBACK_GIVEN, saved.getId(), FEEDBACK_GIVEN_XP);

        if (request.getScore() != null && request.getScore().compareTo(HIGHLY_RATED_THRESHOLD) >= 0) {
            gamificationService.awardBadge(target.getId(), "HIGHLY_RATED");
        }

        eventPublisher.publishEvent(new FeedbackGivenEvent(
                saved.getId(), target.getId(), author.getId(),
                author.getFirstName() + " " + author.getLastName()));

        if (solution != null) {
            eventPublisher.publishEvent(new SolutionReviewedEvent(
                    solution.getId(), target.getId(), solution.getResourceId(), resource.getTitle()));
        }

        return new FeedbackResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getReceivedFeedback(String targetEmail) {
        User target = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + targetEmail));

        return feedbackRepository.findByTargetIdOrderByCreatedAtDesc(target.getId())
                .stream()
                .map(FeedbackResponse::new)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getGivenFeedback(String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + authorEmail));

        return feedbackRepository.findByAuthorIdOrderByCreatedAtDesc(author.getId())
                .stream()
                .map(FeedbackResponse::new)
                .toList();
    }
}
