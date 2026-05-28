package com.mentoredu.catalog.service;

import com.mentoredu.catalog.dto.*;
import com.mentoredu.catalog.exception.*;
import com.mentoredu.catalog.model.*;
import com.mentoredu.catalog.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogService implements ICatalogService {

    private final UniversityRepository universityRepository;
    private final AreaRepository areaRepository;
    private final CourseRepository courseRepository;
    private final CareerRepository careerRepository;

    @Override
    public List<UniversityResponse> listUniversities() {
        return universityRepository.findAll().stream().map(UniversityResponse::from).toList();
    }

    @Override
    @Transactional
    public UniversityResponse createUniversity(CreateUniversityRequest request) {
        if (universityRepository.existsByName(request.name()))
            throw new DuplicateUniversityException("Ya existe una universidad con el nombre: " + request.name());
        University u = University.builder().name(request.name()).city(request.city()).build();
        return UniversityResponse.from(universityRepository.save(u));
    }

    @Override
    public List<AreaResponse> listAreasByUniversity(UUID universityId) {
        if (!universityRepository.existsById(universityId))
            throw new UniversityNotFoundException("Universidad no encontrada: " + universityId);
        return areaRepository.findByUniversityId(universityId).stream().map(AreaResponse::from).toList();
    }

    @Override
    @Transactional
    public AreaResponse createArea(UUID universityId, CreateAreaRequest request) {
        University university = universityRepository.findById(universityId)
            .orElseThrow(() -> new UniversityNotFoundException("Universidad no encontrada: " + universityId));
        if (areaRepository.existsByUniversityIdAndName(universityId, request.name()))
            throw new IllegalArgumentException("Ya existe un área con ese nombre en esta universidad");
        Area area = Area.builder()
            .university(university)
            .name(request.name())
            .description(request.description())
            .courses(new ArrayList<>())
            .build();
        return AreaResponse.from(areaRepository.save(area));
    }

    @Override
    public List<CourseResponse> listCourses() {
        return courseRepository.findAll().stream().map(CourseResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> listCoursesByArea(UUID areaId) {
        Area area = areaRepository.findById(areaId)
            .orElseThrow(() -> new AreaNotFoundException("Área no encontrada: " + areaId));
        return area.getCourses().stream().map(CourseResponse::from).toList();
    }

    @Override
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        if (courseRepository.existsByName(request.name()))
            throw new DuplicateCourseException("Ya existe un curso con el nombre: " + request.name());
        Course course = Course.builder().name(request.name()).description(request.description()).build();
        return CourseResponse.from(courseRepository.save(course));
    }

    @Override
    @Transactional
    public void linkCourseToArea(UUID areaId, UUID courseId) {
        Area area = areaRepository.findById(areaId)
            .orElseThrow(() -> new AreaNotFoundException("Área no encontrada: " + areaId));
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new CourseNotFoundException("Curso no encontrado: " + courseId));
        if (area.getCourses() == null) area.setCourses(new ArrayList<>());
        if (!area.getCourses().contains(course)) area.getCourses().add(course);
        areaRepository.save(area);
    }

    @Override
    public List<CareerResponse> listCareersByUniversity(UUID universityId) {
        if (!universityRepository.existsById(universityId))
            throw new UniversityNotFoundException("Universidad no encontrada: " + universityId);
        return careerRepository.findByUniversityId(universityId).stream().map(CareerResponse::from).toList();
    }

    @Override
    @Transactional
    public CareerResponse createCareer(UUID universityId, CreateCareerRequest request) {
        University university = universityRepository.findById(universityId)
            .orElseThrow(() -> new UniversityNotFoundException("Universidad no encontrada: " + universityId));
        Area area = areaRepository.findById(request.areaId())
            .orElseThrow(() -> new AreaNotFoundException("Área no encontrada: " + request.areaId()));
        if (careerRepository.existsByUniversityIdAndAreaIdAndName(universityId, request.areaId(), request.name()))
            throw new IllegalArgumentException("Ya existe esa carrera en esta universidad y área");
        Career career = Career.builder()
            .university(university)
            .area(area)
            .name(request.name())
            .description(request.description())
            .courses(new ArrayList<>())
            .build();
        return CareerResponse.from(careerRepository.save(career));
    }

    @Override
    @Transactional
    public void linkCourseToCareer(UUID careerId, UUID courseId) {
        Career career = careerRepository.findById(careerId)
            .orElseThrow(() -> new CareerNotFoundException("Carrera no encontrada: " + careerId));
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new CourseNotFoundException("Curso no encontrado: " + courseId));
        if (career.getCourses() == null) career.setCourses(new ArrayList<>());
        if (!career.getCourses().contains(course)) career.getCourses().add(course);
        careerRepository.save(career);
    }

    @Override
    public boolean universityExists(UUID id) {
        return universityRepository.existsById(id);
    }

    @Override
    public boolean areaExists(UUID id) {
        return areaRepository.existsById(id);
    }

    @Override
    public boolean courseExists(UUID id) {
        return courseRepository.existsById(id);
    }

    @Override
    public boolean careerExists(UUID id) {
        return careerRepository.existsById(id);
    }

    @Override
    public boolean areaExistsInUniversity(UUID areaId, UUID universityId) {
        return areaRepository.existsByIdAndUniversityId(areaId, universityId);
    }

    @Override
    public boolean careerExistsInUniversity(UUID careerId, UUID universityId) {
        return careerRepository.existsByIdAndUniversityId(careerId, universityId);
    }

    @Override
    public boolean careerExistsInArea(UUID careerId, UUID areaId) {
        return careerRepository.existsByIdAndAreaId(careerId, areaId);
    }
}
