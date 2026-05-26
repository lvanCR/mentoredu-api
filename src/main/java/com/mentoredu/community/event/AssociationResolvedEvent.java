package com.mentoredu.community.event;

import com.mentoredu.community.model.AssociationStatus;

import java.util.UUID;

public record AssociationResolvedEvent(
        UUID linkId,
        UUID teacherUserId,
        UUID academyProfileId,
        AssociationStatus status
) {}
