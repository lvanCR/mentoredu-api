package com.mentoredu.community.repository;

import com.mentoredu.community.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {
    Optional<Reaction> findByUserIdAndThreadId(UUID userId, UUID threadId);
    Optional<Reaction> findByUserIdAndAnswerId(UUID userId, UUID answerId);
    Optional<Reaction> findByUserIdAndCommentId(UUID userId, UUID commentId);
}
