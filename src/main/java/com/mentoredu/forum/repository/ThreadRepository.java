package com.mentoredu.forum.repository;

import com.mentoredu.forum.model.ForumThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ThreadRepository extends JpaRepository<ForumThread, UUID> {
    Page<ForumThread> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * includeAnonymous=true  → caller is the owner, sees own anonymous threads
     * includeAnonymous=false → third party, anonymous threads hidden (privacy)
     */
    @Query("""
        SELECT t FROM ForumThread t
        WHERE (:authorId IS NULL OR t.author.id = :authorId)
          AND (t.anonymous = false OR :includeAnonymous = true)
        ORDER BY t.createdAt DESC
        """)
    Page<ForumThread> findByAuthorIdOrAll(
        @Param("authorId")        UUID    authorId,
        @Param("includeAnonymous") boolean includeAnonymous,
        Pageable pageable
    );
}
