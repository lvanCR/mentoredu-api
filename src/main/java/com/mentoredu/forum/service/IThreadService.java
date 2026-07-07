package com.mentoredu.forum.service;

import com.mentoredu.config.PagedResponse;
import com.mentoredu.forum.dto.CreateThreadRequest;
import com.mentoredu.forum.dto.ThreadResponse;
import com.mentoredu.forum.dto.UpdateThreadRequest;

import java.util.UUID;

public interface IThreadService {
    ThreadResponse create(CreateThreadRequest request, String authorEmail);
    PagedResponse<ThreadResponse> listRecent(int page, int size, String currentUserEmail, UUID authorId);
    ThreadResponse get(UUID id, String currentUserEmail);
    ThreadResponse update(UUID id, UpdateThreadRequest request, String requesterEmail);
    ThreadResponse close(UUID id, String requesterEmail);
    void delete(UUID id, String requesterEmail);
}
