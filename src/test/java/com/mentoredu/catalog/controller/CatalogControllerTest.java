package com.mentoredu.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.catalog.dto.*;
import com.mentoredu.catalog.exception.*;
import com.mentoredu.catalog.service.ICatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CatalogController.class)
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ICatalogService catalogService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // GET /catalog/universities — público (no requiere auth)
    // =========================================================================

    @Test
    void listUniversities_withoutAuth_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(catalogService.listUniversities())
                .thenReturn(List.of(UniversityResponse.builder().id(id).name("UNMSM").city("Lima").build()));

        mockMvc.perform(get("/api/v1/catalog/universities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("UNMSM"))
                .andExpect(jsonPath("$[0].city").value("Lima"));
    }

    @Test
    void listUniversities_whenEmpty_returns200WithEmptyArray() throws Exception {
        when(catalogService.listUniversities()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/catalog/universities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // =========================================================================
    // POST /catalog/universities — solo ADMIN (US28 / RN-20)
    // =========================================================================

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createUniversity_asAdmin_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        var request = new CreateUniversityRequest("Universidad Nacional Mayor de San Marcos", "Lima");
        var response = UniversityResponse.builder().id(id).name("Universidad Nacional Mayor de San Marcos").city("Lima").build();

        when(catalogService.createUniversity(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/catalog/universities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Universidad Nacional Mayor de San Marcos"))
                .andExpect(jsonPath("$.city").value("Lima"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createUniversity_withBlankName_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/catalog/universities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"city\":\"Lima\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.name").exists());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createUniversity_withDuplicateName_returns409() throws Exception {
        var request = new CreateUniversityRequest("UNMSM", "Lima");
        when(catalogService.createUniversity(any()))
                .thenThrow(new DuplicateUniversityException("Universidad ya registrada: UNMSM"));

        mockMvc.perform(post("/api/v1/catalog/universities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Universidad ya registrada: UNMSM"));
    }

    // =========================================================================
    // GET /catalog/universities/{id}/areas — público
    // =========================================================================

    @Test
    void listAreas_withoutAuth_returns200() throws Exception {
        UUID universityId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        when(catalogService.listAreasByUniversity(eq(universityId)))
                .thenReturn(List.of(AreaResponse.builder().id(areaId).universityId(universityId).name("Ciencias").build()));

        mockMvc.perform(get("/api/v1/catalog/universities/{id}/areas", universityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(areaId.toString()))
                .andExpect(jsonPath("$[0].universityId").value(universityId.toString()))
                .andExpect(jsonPath("$[0].name").value("Ciencias"));
    }

    @Test
    void listAreas_whenUniversityNotFound_returns404() throws Exception {
        UUID universityId = UUID.randomUUID();
        when(catalogService.listAreasByUniversity(eq(universityId)))
                .thenThrow(new UniversityNotFoundException("Universidad no encontrada: " + universityId));

        mockMvc.perform(get("/api/v1/catalog/universities/{id}/areas", universityId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // =========================================================================
    // POST /catalog/universities/{id}/areas — solo ADMIN
    // =========================================================================

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createArea_asAdmin_returns201() throws Exception {
        UUID universityId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        var request = new CreateAreaRequest("Ciencias de la Computación", "Área de sistemas e informática");
        var response = AreaResponse.builder().id(areaId).universityId(universityId).name("Ciencias de la Computación").build();

        when(catalogService.createArea(eq(universityId), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/catalog/universities/{id}/areas", universityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(areaId.toString()))
                .andExpect(jsonPath("$.name").value("Ciencias de la Computación"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createArea_whenUniversityNotFound_returns404() throws Exception {
        UUID universityId = UUID.randomUUID();
        var request = new CreateAreaRequest("Ingeniería", null);

        when(catalogService.createArea(eq(universityId), any()))
                .thenThrow(new UniversityNotFoundException("Universidad no encontrada: " + universityId));

        mockMvc.perform(post("/api/v1/catalog/universities/{id}/areas", universityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createArea_withBlankName_returns400() throws Exception {
        UUID universityId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/catalog/universities/{id}/areas", universityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.name").exists());
    }

    // =========================================================================
    // GET /catalog/courses — público
    // =========================================================================

    @Test
    void listCourses_withoutAuth_returns200() throws Exception {
        UUID courseId = UUID.randomUUID();
        when(catalogService.listCourses())
                .thenReturn(List.of(CourseResponse.builder().id(courseId).name("Cálculo I").build()));

        mockMvc.perform(get("/api/v1/catalog/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Cálculo I"));
    }

    // =========================================================================
    // GET /catalog/areas/{areaId}/courses
    // =========================================================================

    @Test
    void listCoursesByArea_withoutAuth_returns200() throws Exception {
        UUID areaId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        when(catalogService.listCoursesByArea(eq(areaId)))
                .thenReturn(List.of(CourseResponse.builder().id(courseId).name("Álgebra Lineal").build()));

        mockMvc.perform(get("/api/v1/catalog/areas/{areaId}/courses", areaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Álgebra Lineal"));
    }

    @Test
    void listCoursesByArea_whenAreaNotFound_returns404() throws Exception {
        UUID areaId = UUID.randomUUID();
        when(catalogService.listCoursesByArea(eq(areaId)))
                .thenThrow(new AreaNotFoundException("Área no encontrada: " + areaId));

        mockMvc.perform(get("/api/v1/catalog/areas/{areaId}/courses", areaId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // =========================================================================
    // POST /catalog/courses — solo ADMIN
    // =========================================================================

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createCourse_asAdmin_returns201() throws Exception {
        UUID courseId = UUID.randomUUID();
        var request = new CreateCourseRequest("Probabilidad y Estadística", null);
        var response = CourseResponse.builder().id(courseId).name("Probabilidad y Estadística").build();

        when(catalogService.createCourse(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/catalog/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(courseId.toString()))
                .andExpect(jsonPath("$.name").value("Probabilidad y Estadística"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createCourse_withBlankName_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/catalog/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.name").exists());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createCourse_withDuplicateName_returns409() throws Exception {
        var request = new CreateCourseRequest("Cálculo I", null);
        when(catalogService.createCourse(any()))
                .thenThrow(new DuplicateCourseException("Curso ya registrado: Cálculo I"));

        mockMvc.perform(post("/api/v1/catalog/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Curso ya registrado: Cálculo I"));
    }

    // =========================================================================
    // POST /catalog/areas/{areaId}/courses/{courseId} — solo ADMIN
    // =========================================================================

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void linkCourseToArea_asAdmin_returns204() throws Exception {
        UUID areaId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/catalog/areas/{areaId}/courses/{courseId}", areaId, courseId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void linkCourseToArea_whenAreaNotFound_returns404() throws Exception {
        UUID areaId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        doThrow(new AreaNotFoundException("Área no encontrada: " + areaId))
                .when(catalogService).linkCourseToArea(eq(areaId), eq(courseId));

        mockMvc.perform(put("/api/v1/catalog/areas/{areaId}/courses/{courseId}", areaId, courseId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // =========================================================================
    // GET /catalog/universities/{id}/careers — público
    // =========================================================================

    @Test
    void listCareers_withoutAuth_returns200() throws Exception {
        UUID universityId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        when(catalogService.listCareersByUniversity(eq(universityId)))
                .thenReturn(List.of(CareerResponse.builder().id(careerId).universityId(universityId).areaId(areaId).name("Ingeniería de Sistemas").build()));

        mockMvc.perform(get("/api/v1/catalog/universities/{id}/careers", universityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Ingeniería de Sistemas"));
    }

    @Test
    void listCareers_whenUniversityNotFound_returns404() throws Exception {
        UUID universityId = UUID.randomUUID();
        when(catalogService.listCareersByUniversity(eq(universityId)))
                .thenThrow(new UniversityNotFoundException("Universidad no encontrada: " + universityId));

        mockMvc.perform(get("/api/v1/catalog/universities/{id}/careers", universityId))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // POST /catalog/universities/{id}/careers — solo ADMIN
    // =========================================================================

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createCareer_asAdmin_returns201() throws Exception {
        UUID universityId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        var request = new CreateCareerRequest(areaId, "Ingeniería de Sistemas", null);
        var response = CareerResponse.builder().id(careerId).universityId(universityId).areaId(areaId).name("Ingeniería de Sistemas").build();

        when(catalogService.createCareer(eq(universityId), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/catalog/universities/{id}/careers", universityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(careerId.toString()))
                .andExpect(jsonPath("$.name").value("Ingeniería de Sistemas"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createCareer_whenUniversityNotFound_returns404() throws Exception {
        UUID universityId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        var request = new CreateCareerRequest(areaId, "Medicina", null);

        when(catalogService.createCareer(eq(universityId), any()))
                .thenThrow(new UniversityNotFoundException("Universidad no encontrada: " + universityId));

        mockMvc.perform(post("/api/v1/catalog/universities/{id}/careers", universityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createCareer_withBlankName_returns400() throws Exception {
        UUID universityId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/catalog/universities/{id}/careers", universityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"areaId\":\"" + areaId + "\",\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.name").exists());
    }

    // =========================================================================
    // POST /catalog/careers/{careerId}/courses/{courseId} — solo ADMIN
    // =========================================================================

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void linkCourseToCareer_asAdmin_returns204() throws Exception {
        UUID careerId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/catalog/careers/{careerId}/courses/{courseId}", careerId, courseId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void linkCourseToCareer_whenCareerNotFound_returns404() throws Exception {
        UUID careerId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        doThrow(new CareerNotFoundException("Carrera no encontrada: " + careerId))
                .when(catalogService).linkCourseToCareer(eq(careerId), eq(courseId));

        mockMvc.perform(put("/api/v1/catalog/careers/{careerId}/courses/{courseId}", careerId, courseId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

}
