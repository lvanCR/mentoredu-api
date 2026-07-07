package com.mentoredu.forum.service;

import com.mentoredu.config.PagedResponse;
import com.mentoredu.forum.dto.AnswerResponse;
import com.mentoredu.forum.dto.CreateAnswerRequest;
import com.mentoredu.forum.dto.UpdateBodyRequest;

import java.util.UUID;

public interface IAnswerService {
    AnswerResponse create(UUID threadId, CreateAnswerRequest request, String authorEmail);
    PagedResponse<AnswerResponse> listByThread(UUID threadId, int page, int size, String currentUserEmail);
    AnswerResponse update(UUID threadId, UUID answerId, UpdateBodyRequest request, String requesterEmail);
    void delete(UUID threadId, UUID answerId, String requesterEmail);
}
