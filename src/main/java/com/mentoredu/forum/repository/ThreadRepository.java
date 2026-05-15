package com.mentoredu.forum.repository;

import com.mentoredu.forum.model.Thread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ThreadRepository extends JpaRepository<Thread, UUID> {
    Page<Thread> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
