package com.mentoredu.community.event;

import java.util.UUID;

public record AssociationResolvedEvent(
        UUID linkId,
        UUID teacherUserId,
        UUID academyProfileId,
        String status
) {}
