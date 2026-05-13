package com.mentoredu.community.repository;

import com.mentoredu.community.model.ThreadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ThreadRepository extends JpaRepository<ThreadEntity, UUID> {
}
