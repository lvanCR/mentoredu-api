package com.mentoredu.forum.repository;

import com.mentoredu.auth.model.User;
import com.mentoredu.forum.model.FollowRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<FollowRelation, UUID> {
    boolean existsByFollowerAndFollowed(User follower, User followed);
    Optional<FollowRelation> findByFollowerAndFollowed(User follower, User followed);
    List<FollowRelation> findByFollower(User follower);
    List<FollowRelation> findByFollowed(User followed);
}
