package com.mentoredu.forum.repository;

import com.mentoredu.forum.model.ForumThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ThreadRepository extends JpaRepository<ForumThread, UUID> {
    Page<ForumThread> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
