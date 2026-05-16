package com.mentoredu.gamification.repository;

import com.mentoredu.gamification.model.LevelProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LevelProgressRepository extends JpaRepository<LevelProgress, UUID> {
}
