package com.mentoredu.community.event;

import java.util.UUID;

public record UserFollowedEvent(
        UUID followerId,
        UUID followedId
) {}
