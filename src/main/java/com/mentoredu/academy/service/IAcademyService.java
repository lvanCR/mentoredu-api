package com.mentoredu.academy.service;

import com.mentoredu.academy.dto.AcademyResponse;
import com.mentoredu.academy.dto.CreateAcademyRequest;
import com.mentoredu.academy.dto.CreateCycleRequest;
import com.mentoredu.academy.dto.CreateProgramRequest;
import com.mentoredu.academy.dto.CycleResponse;
import com.mentoredu.academy.dto.ProgramResponse;

import java.util.UUID;

public interface IAcademyService {
    AcademyResponse createAcademy(String email, CreateAcademyRequest request);

    // US11
    ProgramResponse createProgram(String email, UUID academyId, CreateProgramRequest request);
    CycleResponse   createCycle(String email, UUID academyId, CreateCycleRequest request);
}
