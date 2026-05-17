package com.mentoredu.forum.service;

import com.mentoredu.forum.dto.AnswerResponse;
import com.mentoredu.forum.dto.CreateAnswerRequest;

import java.util.List;
import java.util.UUID;

public interface IAnswerService {
    AnswerResponse create(UUID threadId, CreateAnswerRequest request, String authorEmail);
    List<AnswerResponse> listByThread(UUID threadId);
}
