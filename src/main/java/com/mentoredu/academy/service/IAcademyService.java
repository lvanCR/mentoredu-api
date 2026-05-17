package com.mentoredu.academy.service;

import com.mentoredu.academy.dto.AcademyResponse;
import com.mentoredu.academy.dto.CreateAcademyRequest;

public interface IAcademyService {
    AcademyResponse createAcademy(String email, CreateAcademyRequest request);
}
