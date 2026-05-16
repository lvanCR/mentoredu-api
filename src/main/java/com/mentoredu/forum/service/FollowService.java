package com.mentoredu.forum.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.forum.dto.FollowResponse;
import com.mentoredu.forum.model.FollowRelation;
import com.mentoredu.forum.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService implements IFollowService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    @Override
    @Transactional
    public void follow(UUID followerId, UUID followedId) {
        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("No puedes seguirte a ti mismo");
        }
        User follower = getUserOrThrow(followerId);
        User followed = getUserOrThrow(followedId);
        if (followRepository.existsByFollowerAndFollowed(follower, followed)) {
            throw new IllegalArgumentException("Ya sigues a este usuario");
        }
        followRepository.save(FollowRelation.builder()
                .follower(follower)
                .followed(followed)
                .build());
    }

    @Override
    @Transactional
    public void unfollow(UUID followerId, UUID followedId) {
        User follower = getUserOrThrow(followerId);
        User followed = getUserOrThrow(followedId);
        FollowRelation relation = followRepository.findByFollowerAndFollowed(follower, followed)
                .orElseThrow(() -> new IllegalArgumentException("No sigues a este usuario"));
        followRepository.delete(relation);
    }

    @Override
    public List<FollowResponse> getFollowing(UUID userId) {
        User user = getUserOrThrow(userId);
        return followRepository.findByFollower(user).stream()
                .map(f -> new FollowResponse(
                        f.getFollowed().getId(),
                        f.getFollowed().getFirstName(),
                        f.getFollowed().getLastName(),
                        f.getFollowed().getEmail()))
                .collect(Collectors.toList());
    }

    @Override
    public List<FollowResponse> getFollowers(UUID userId) {
        User user = getUserOrThrow(userId);
        return followRepository.findByFollowed(user).stream()
                .map(f -> new FollowResponse(
                        f.getFollower().getId(),
                        f.getFollower().getFirstName(),
                        f.getFollower().getLastName(),
                        f.getFollower().getEmail()))
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario inexistente"));
    }
}
