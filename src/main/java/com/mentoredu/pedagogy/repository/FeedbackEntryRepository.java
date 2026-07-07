package com.mentoredu.pedagogy.repository;

import com.mentoredu.pedagogy.model.FeedbackEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackEntryRepository extends JpaRepository<FeedbackEntry, UUID> {
    Optional<FeedbackEntry> findBySolutionId(UUID solutionId);
    boolean existsBySolutionId(UUID solutionId);
    List<FeedbackEntry> findBySolution_IdIn(Collection<UUID> solutionIds);

    @Modifying
    void deleteBySolutionId(UUID solutionId);

    @Modifying
    void deleteBySolution_IdIn(Collection<UUID> solutionIds);
}
