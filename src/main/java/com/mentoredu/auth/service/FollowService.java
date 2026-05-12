package com.mentoredu.auth.service;

import com.mentoredu.auth.model.Follow;
import com.mentoredu.auth.model.User;
import com.mentoredu.auth.repository.FollowRepository;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.auth.dto.FollowResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FollowRepository followRepository;

    @Transactional
    public void follow(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("No puedes seguirte a ti mismo");
        }
        User follower = getUserOrThrow(followerId);
        User followed = getUserOrThrow(followedId);
        if (followRepository.existsByFollowerAndFollowed(follower, followed)) {
            throw new IllegalArgumentException("Ya sigues a este usuario");
        }
        Follow follow = Follow.builder()
                .follower(follower)
                .followed(followed)
                .createdAt(LocalDateTime.now())
                .build();
        followRepository.save(follow);
    }

    @Transactional
    public void unfollow(Long followerId, Long followedId) {
        User follower = getUserOrThrow(followerId);
        User followed = getUserOrThrow(followedId);
        Follow follow = followRepository.findByFollowerAndFollowed(follower, followed)
                .orElseThrow(() -> new IllegalArgumentException("No sigues a este usuario"));
        followRepository.delete(follow);
    }

    public List<FollowResponse> getFollowing(Long userId) {
        User user = getUserOrThrow(userId);
        return followRepository.findByFollower(user).stream()
                .map(f -> new FollowResponse(
                        f.getFollowed().getId(),
                        f.getFollowed().getFirstName(),
                        f.getFollowed().getLastName(),
                        f.getFollowed().getEmail()
                )).collect(Collectors.toList());
    }

    public List<FollowResponse> getFollowers(Long userId) {
        User user = getUserOrThrow(userId);
        return followRepository.findByFollowed(user).stream()
                .map(f -> new FollowResponse(
                        f.getFollower().getId(),
                        f.getFollower().getFirstName(),
                        f.getFollower().getLastName(),
                        f.getFollower().getEmail()
                )).collect(Collectors.toList());
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario inexistente"));
    }
}
