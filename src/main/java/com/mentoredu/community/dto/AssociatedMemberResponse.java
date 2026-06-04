package com.mentoredu.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Respuesta pública enriquecida para mostrar el equipo docente de una academia
 * o las academias asociadas a un docente.
 */
public record AssociatedMemberResponse(
    UUID    profileId,
    UUID    userId,
    String  displayName,
    String  avatarUrl,
    String  profileType,
    @JsonProperty("isVerified") boolean isVerified
) {}
