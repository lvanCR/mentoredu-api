package com.mentoredu.forum.repository;

import com.mentoredu.forum.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findAllByAnswer_IdOrderByCreatedAtAsc(UUID answerId);
}
