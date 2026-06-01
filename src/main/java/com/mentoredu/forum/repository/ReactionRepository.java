package com.mentoredu.forum.repository;

import com.mentoredu.forum.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {

    Optional<Reaction> findByUserIdAndTargetTypeAndTargetId(UUID userId, String targetType, UUID targetId);

    long countByTargetTypeAndTargetId(String targetType, UUID targetId);

    long countByTargetTypeAndTargetIdAndReactionType(String targetType, UUID targetId, String reactionType);
}
