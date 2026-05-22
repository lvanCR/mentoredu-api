package com.mentoredu.catalog.controller;

import com.mentoredu.catalog.dto.*;
import com.mentoredu.catalog.service.ICatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final ICatalogService catalogService;

    // ── Universities ──────────────────────────────────────
    @GetMapping("/universities")
    public ResponseEntity<List<UniversityResponse>> listUniversities() {
        return ResponseEntity.ok(catalogService.listUniversities());
    }

    @PostMapping("/universities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UniversityResponse> createUniversity(@Valid @RequestBody CreateUniversityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createUniversity(request));
    }

    // ── Areas ──────────────────────────────────────────────
    @GetMapping("/universities/{universityId}/areas")
    public ResponseEntity<List<AreaResponse>> listAreas(@PathVariable UUID universityId) {
        return ResponseEntity.ok(catalogService.listAreasByUniversity(universityId));
    }

    @PostMapping("/universities/{universityId}/areas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AreaResponse> createArea(
            @PathVariable UUID universityId,
            @Valid @RequestBody CreateAreaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createArea(universityId, request));
    }

    // ── Courses ────────────────────────────────────────────
    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> listCourses() {
        return ResponseEntity.ok(catalogService.listCourses());
    }

    @GetMapping("/areas/{areaId}/courses")
    public ResponseEntity<List<CourseResponse>> listCoursesByArea(@PathVariable UUID areaId) {
        return ResponseEntity.ok(catalogService.listCoursesByArea(areaId));
    }

    @PostMapping("/courses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createCourse(request));
    }

    @PostMapping("/areas/{areaId}/courses/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> linkCourseToArea(@PathVariable UUID areaId, @PathVariable UUID courseId) {
        catalogService.linkCourseToArea(areaId, courseId);
        return ResponseEntity.noContent().build();
    }

    // ── Careers ────────────────────────────────────────────
    @GetMapping("/universities/{universityId}/careers")
    public ResponseEntity<List<CareerResponse>> listCareers(@PathVariable UUID universityId) {
        return ResponseEntity.ok(catalogService.listCareersByUniversity(universityId));
    }

    @PostMapping("/universities/{universityId}/careers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CareerResponse> createCareer(
            @PathVariable UUID universityId,
            @Valid @RequestBody CreateCareerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createCareer(universityId, request));
    }

    @PostMapping("/careers/{careerId}/courses/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> linkCourseToCareer(@PathVariable UUID careerId, @PathVariable UUID courseId) {
        catalogService.linkCourseToCareer(careerId, courseId);
        return ResponseEntity.noContent().build();
    }
}
