package com.mentoredu.feedback.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.feedback.dto.FeedbackRequest;
import com.mentoredu.feedback.dto.FeedbackResponse;
import com.mentoredu.feedback.exception.FeedbackTargetNotFoundException;
import com.mentoredu.feedback.exception.FeedbackUnauthorizedException;
import com.mentoredu.feedback.model.FeedbackEntry;
import com.mentoredu.feedback.repository.FeedbackRepository;
import com.mentoredu.forum.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService implements IFeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

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

        FeedbackEntry entry = FeedbackEntry.builder()
                .author(author)
                .target(target)
                .programId(request.getProgramId())
                .cycleId(request.getCycleId())
                .body(request.getBody())
                .score(request.getScore())
                .build();

        return new FeedbackResponse(feedbackRepository.save(entry));
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
}
