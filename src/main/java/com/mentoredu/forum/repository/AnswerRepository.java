package com.mentoredu.forum.repository;

import com.mentoredu.forum.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {
    List<Answer> findAllByThread_IdOrderByCreatedAtAsc(UUID threadId);
}
