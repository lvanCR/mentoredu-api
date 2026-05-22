package com.mentoredu.pedagogy.repository;

import com.mentoredu.pedagogy.model.FeedbackEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FeedbackEntryRepository extends JpaRepository<FeedbackEntry, UUID> {
    Optional<FeedbackEntry> findBySolutionId(UUID solutionId);
    boolean existsBySolutionId(UUID solutionId);
}
