package com.mentoredu.forum.service;

import com.mentoredu.forum.dto.FollowResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IFollowService {
    Optional<FollowResponse> toggleFollow(UUID targetUserId, String currentUserEmail);
    List<FollowResponse> getFollowing(UUID userId);
    List<FollowResponse> getFollowers(UUID userId);
}
