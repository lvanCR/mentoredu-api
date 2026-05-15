package com.mentoredu.forum.repository;

import com.mentoredu.forum.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {
}
