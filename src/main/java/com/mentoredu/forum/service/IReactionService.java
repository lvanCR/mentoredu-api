package com.mentoredu.forum.service;

import com.mentoredu.forum.dto.CreateReactionRequest;
import com.mentoredu.forum.dto.ReactionResponse;

import java.util.Optional;
import java.util.UUID;

public interface IReactionService {

    Optional<ReactionResponse> reactToThread(UUID threadId, CreateReactionRequest request, String userEmail);

    Optional<ReactionResponse> reactToAnswer(UUID answerId, CreateReactionRequest request, String userEmail);

    Optional<ReactionResponse> reactToComment(UUID commentId, CreateReactionRequest request, String userEmail);
}
