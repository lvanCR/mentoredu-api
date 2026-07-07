package com.mentoredu.forum.repository;

import com.mentoredu.forum.model.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {
    Page<Answer> findAllByThread_IdOrderByCreatedAtAsc(UUID threadId, Pageable pageable);
    List<Answer> findAllByThread_Id(UUID threadId);
}
