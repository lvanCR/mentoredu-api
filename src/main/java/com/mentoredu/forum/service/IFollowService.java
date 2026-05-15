package com.mentoredu.forum.service;

import com.mentoredu.forum.dto.FollowResponse;

import java.util.List;
import java.util.UUID;

public interface IFollowService {
    void follow(UUID followerId, UUID followedId);
    void unfollow(UUID followerId, UUID followedId);
    List<FollowResponse> getFollowing(UUID userId);
    List<FollowResponse> getFollowers(UUID userId);
}
