package com.mentoredu.community.repository;

import com.mentoredu.community.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByThreadIdOrderByCreatedAtAsc(UUID threadId);
    List<Comment> findByAnswerIdOrderByCreatedAtAsc(UUID answerId);
}
