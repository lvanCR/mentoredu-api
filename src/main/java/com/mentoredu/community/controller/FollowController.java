package com.mentoredu.community.controller;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.community.exception.AlreadyFollowingException;
import com.mentoredu.community.exception.SelfFollowException;
import com.mentoredu.community.model.Follow;
import com.mentoredu.community.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @PostMapping("/{userId}/follow")
    public ResponseEntity<Void> toggleFollow(@PathVariable UUID userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        User follower = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        if (follower.getId().equals(userId))
            throw new SelfFollowException("No puedes seguirte a ti mismo");

        User followed = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario objetivo no encontrado"));

        Optional<Follow> existing = followRepository.findByFollowerIdAndFollowedId(follower.getId(), followed.getId());
        if (existing.isPresent()) {
            followRepository.delete(existing.get());
            return ResponseEntity.noContent().build();
        }
        Follow follow = Follow.builder().follower(follower).followed(followed).build();
        followRepository.save(follow);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
