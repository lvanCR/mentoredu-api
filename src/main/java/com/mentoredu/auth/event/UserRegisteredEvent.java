package com.mentoredu.auth.event;

import java.util.UUID;

public record UserRegisteredEvent(UUID userId, String firstName, String lastName, String role) {}
