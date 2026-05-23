package com.mentoredu.community.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.community.event.UserFollowedEvent;
import com.mentoredu.community.exception.SelfFollowException;
import com.mentoredu.forum.exception.UserNotFoundException;
import com.mentoredu.community.model.Follow;
import com.mentoredu.community.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowService implements IFollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public boolean toggleFollow(UUID targetUserId, String followerEmail) {
        User follower = userRepository.findByEmail(followerEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + followerEmail));

        if (follower.getId().equals(targetUserId)) {
            throw new SelfFollowException("No puedes seguirte a ti mismo (RN-21)");
        }

        User followed = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + targetUserId));

        Optional<Follow> existing = followRepository.findByFollowerIdAndFollowedId(follower.getId(), followed.getId());

        if (existing.isPresent()) {
            followRepository.delete(existing.get());
            return false;
        }

        Follow follow = Follow.builder()
                .follower(follower)
                .followed(followed)
                .build();
        followRepository.save(follow);
        eventPublisher.publishEvent(new UserFollowedEvent(follower.getId(), followed.getId()));
        return true;
    }
}
