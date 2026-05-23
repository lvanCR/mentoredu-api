package com.mentoredu.community.repository;

import com.mentoredu.community.model.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
    Optional<Follow> findByFollowerIdAndFollowedId(UUID followerId, UUID followedId);
    boolean existsByFollowerIdAndFollowedId(UUID followerId, UUID followedId);
    long countByFollowedId(UUID followedId);
    long countByFollowerId(UUID followerId);
}
