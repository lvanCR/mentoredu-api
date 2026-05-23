package com.mentoredu.catalog.service;

import com.mentoredu.catalog.dto.*;

import java.util.List;
import java.util.UUID;

public interface ICatalogService {
    List<UniversityResponse> listUniversities();
    UniversityResponse createUniversity(CreateUniversityRequest request);

    List<AreaResponse> listAreasByUniversity(UUID universityId);
    AreaResponse createArea(UUID universityId, CreateAreaRequest request);

    List<CourseResponse> listCourses();
    List<CourseResponse> listCoursesByArea(UUID areaId);
    CourseResponse createCourse(CreateCourseRequest request);
    void linkCourseToArea(UUID areaId, UUID courseId);

    List<CareerResponse> listCareersByUniversity(UUID universityId);
    CareerResponse createCareer(UUID universityId, CreateCareerRequest request);
    void linkCourseToCareer(UUID careerId, UUID courseId);
}
