package com.mentoredu.forum.controller;

import com.mentoredu.forum.dto.FollowResponse;
import com.mentoredu.forum.service.IFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/follows")
@RequiredArgsConstructor
public class FollowController {

    private final IFollowService followService;

    @PostMapping("/{followerId}/follow/{followedId}")
    public ResponseEntity<Void> follow(@PathVariable UUID followerId, @PathVariable UUID followedId) {
        followService.follow(followerId, followedId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{followerId}/unfollow/{followedId}")
    public ResponseEntity<Void> unfollow(@PathVariable UUID followerId, @PathVariable UUID followedId) {
        followService.unfollow(followerId, followedId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<List<FollowResponse>> getFollowing(@PathVariable UUID userId) {
        return ResponseEntity.ok(followService.getFollowing(userId));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<FollowResponse>> getFollowers(@PathVariable UUID userId) {
        return ResponseEntity.ok(followService.getFollowers(userId));
    }
}
