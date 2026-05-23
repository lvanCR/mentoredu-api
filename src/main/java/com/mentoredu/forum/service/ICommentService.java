package com.mentoredu.forum.service;

import com.mentoredu.forum.dto.CommentResponse;
import com.mentoredu.forum.dto.CreateCommentRequest;

import java.util.List;
import java.util.UUID;

public interface ICommentService {
    CommentResponse create(UUID answerId, CreateCommentRequest request, String authorEmail);
    List<CommentResponse> listByAnswer(UUID answerId);
}
