package com.mentoredu.pedagogy.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.library.model.Resource;
import com.mentoredu.library.repository.ResourceRepository;
import com.mentoredu.community.repository.TeacherAcademyLinkRepository;
import com.mentoredu.library.exception.ResourceNotFoundException;
import com.mentoredu.pedagogy.dto.CreateFeedbackRequest;
import com.mentoredu.profile.repository.AcademyProfileRepository;
import com.mentoredu.profile.repository.TeacherProfileRepository;
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
    private final UserRepository userRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final AcademyProfileRepository academyProfileRepository;
    private final TeacherAcademyLinkRepository teacherAcademyLinkRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public FeedbackResponse create(UUID solutionId, CreateFeedbackRequest request, String authorEmail) {
        Solution solution = solutionRepository.findById(solutionId)
            .orElseThrow(() -> new SolutionNotFoundException("Resolución no encontrada: " + solutionId));
        if (feedbackEntryRepository.existsBySolutionId(solutionId))
            throw new FeedbackAlreadyExistsException("Ya existe feedback para esta resolución");
        Resource resource = resourceRepository.findById(solution.getResourceId())
            .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));
        User author = userRepository.findByEmail(authorEmail)
            .orElseThrow(() -> new SolutionAccessDeniedException("Usuario no encontrado"));
        // RN-10: autor directo, o TEACHER con link ACCEPTED a la academia autora
        if (!isAuthorizedForResource(resource, author))
            throw new SolutionAccessDeniedException("Solo el autor del ejercicio puede dar feedback");
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
        return FeedbackResponse.from(saved);
    }

    // RN-10: (a) autor directo, o (b) TEACHER con link ACCEPTED a la academia autora
    private boolean isAuthorizedForResource(Resource resource, User requester) {
        if (resource.getAuthor().getId().equals(requester.getId())) return true;
        var academyProfile = academyProfileRepository.findByProfile_UserId(resource.getAuthor().getId());
        if (academyProfile.isEmpty()) return false;
        var teacherProfile = teacherProfileRepository.findByProfile_UserId(requester.getId());
        if (teacherProfile.isEmpty()) return false;
        return teacherAcademyLinkRepository
            .findByTeacherProfileIdAndAcademyProfileId(
                teacherProfile.get().getProfileId(), academyProfile.get().getProfileId())
            .map(link -> "ACCEPTED".equals(link.getStatus()))
            .orElse(false);
    }

    @Override
    public FeedbackResponse getBySolution(UUID solutionId) {
        FeedbackEntry feedback = feedbackEntryRepository.findBySolutionId(solutionId)
            .orElseThrow(() -> new FeedbackNotFoundException("No hay feedback para esta resolución"));
        return FeedbackResponse.from(feedback);
    }
}
