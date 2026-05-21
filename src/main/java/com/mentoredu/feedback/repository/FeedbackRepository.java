package com.mentoredu.feedback.repository;

import com.mentoredu.feedback.model.FeedbackEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackRepository extends JpaRepository<FeedbackEntry, UUID> {
    List<FeedbackEntry> findByTargetIdOrderByCreatedAtDesc(UUID targetUserId);
    List<FeedbackEntry> findByAuthorIdOrderByCreatedAtDesc(UUID authorId);
    Optional<FeedbackEntry> findBySolutionId(UUID solutionId);
}
