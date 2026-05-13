package com.mentoredu.community.repository;

import com.mentoredu.community.model.AnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<AnswerEntity, UUID> {
    List<AnswerEntity> findByThreadId(UUID threadId);
}
