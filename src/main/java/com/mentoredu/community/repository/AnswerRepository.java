package com.mentoredu.community.repository;

import com.mentoredu.community.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {
    List<Answer> findByThreadId(UUID threadId);
}
