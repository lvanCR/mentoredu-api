package com.mentoredu.forum.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.forum.dto.FollowResponse;
import com.mentoredu.forum.exception.UserNotFoundException;
import com.mentoredu.forum.model.FollowRelation;
import com.mentoredu.forum.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService implements IFollowService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    @Override
    @Transactional
    public Optional<FollowResponse> toggleFollow(UUID targetUserId, String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentUserEmail));

        if (currentUser.getId().equals(targetUserId)) {
            throw new IllegalArgumentException("No puedes seguirte a ti mismo");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + targetUserId));

        Optional<FollowRelation> existing = followRepository.findByFollowerAndFollowed(currentUser, targetUser);
        if (existing.isPresent()) {
            followRepository.delete(existing.get());
            return Optional.empty();
        }

        followRepository.save(FollowRelation.builder()
                .follower(currentUser)
                .followed(targetUser)
                .build());

        return Optional.of(new FollowResponse(
                targetUser.getId(),
                targetUser.getFirstName(),
                targetUser.getLastName(),
                targetUser.getEmail()));
    }

    @Override
    public List<FollowResponse> getFollowing(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        return followRepository.findByFollowed(user).stream()
                .map(f -> new FollowResponse(
                        f.getFollower().getId(),
                        f.getFollower().getFirstName(),
                        f.getFollower().getLastName(),
                        f.getFollower().getEmail()))
                .collect(Collectors.toList());
    }
}
