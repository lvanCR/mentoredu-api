package com.mentoredu.community.service;

import com.mentoredu.community.dto.CreateThreadRequest;
import com.mentoredu.community.dto.ThreadResponse;

import java.util.List;
import java.util.UUID;

public interface IThreadService {
    ThreadResponse create(CreateThreadRequest request, String authorEmail);
    List<ThreadResponse> listRecent(int page, int size);
    ThreadResponse get(UUID id);
}
