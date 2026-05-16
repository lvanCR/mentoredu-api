package com.mentoredu.gamification.repository;

import com.mentoredu.gamification.model.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserBadgeRepository extends JpaRepository<UserBadge, UserBadge.UserBadgeId> {
    List<UserBadge> findByUserId(UUID userId);
}
