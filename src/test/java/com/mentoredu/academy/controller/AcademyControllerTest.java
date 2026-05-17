package com.mentoredu.academy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.academy.dto.AcademyResponse;
import com.mentoredu.academy.dto.CreateAcademyRequest;
import com.mentoredu.academy.exception.AcademyAlreadyExistsException;
import com.mentoredu.academy.model.Academy;
import com.mentoredu.academy.service.IAcademyService;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.profile.exception.ProfileNotFoundException;
import com.mentoredu.profile.exception.WrongProfileTypeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AcademyController.class)
class AcademyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IAcademyService academyService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // US33 — Create academy
    // =========================================================================

    // -------------------------------------------------------------------------
    // Escenario exitoso: campos obligatorios → 201
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "org@example.com")
    void createAcademy_withRequiredFields_returns201() throws Exception {
        var request = academyRequest("Academia Preuniversitaria Norte", null, null, null);
        var response = buildAcademyResponse(
                "Academia Preuniversitaria Norte", null, null, null);
        when(academyService.createAcademy(eq("org@example.com"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/academies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.ownerProfileId").exists())
                .andExpect(jsonPath("$.name").value("Academia Preuniversitaria Norte"))
                .andExpect(jsonPath("$.active").value(true));
    }

    // -------------------------------------------------------------------------
    // Escenario alternativo exitoso: todos los campos → 201
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "org@example.com")
    void createAcademy_withAllFields_returns201() throws Exception {
        var request = academyRequest(
                "Academia Preuniversitaria Norte",
                "Centro de preparación universitaria en Lima Norte.",
                "https://academia-norte.pe",
                "info@academia-norte.pe");
        var response = buildAcademyResponse(
                "Academia Preuniversitaria Norte",
                "Centro de preparación universitaria en Lima Norte.",
                "https://academia-norte.pe",
                "info@academia-norte.pe");
        when(academyService.createAcademy(eq("org@example.com"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/academies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Academia Preuniversitaria Norte"))
                .andExpect(jsonPath("$.description").value("Centro de preparación universitaria en Lima Norte."))
                .andExpect(jsonPath("$.website").value("https://academia-norte.pe"))
                .andExpect(jsonPath("$.email").value("info@academia-norte.pe"))
                .andExpect(jsonPath("$.active").value(true));
    }

    // -------------------------------------------------------------------------
    // Escenario error: name vacío → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "org@example.com")
    void createAcademy_withBlankName_returns400() throws Exception {
        var request = academyRequest("", null, null, null);

        mockMvc.perform(post("/api/v1/academies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.name").exists());
    }

    @Test
    @WithMockUser(username = "org@example.com")
    void createAcademy_withMissingName_returns400() throws Exception {
        String body = "{\"description\": \"Descripción de prueba\"}";

        mockMvc.perform(post("/api/v1/academies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.name").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario alternativo error: academia duplicada por misma organización → 409
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "org@example.com")
    void createAcademy_whenNameAlreadyExistsForSameOrg_returns409() throws Exception {
        when(academyService.createAcademy(eq("org@example.com"), any()))
                .thenThrow(new AcademyAlreadyExistsException(
                        "An academy with this name already exists for this organization: Academia Preuniversitaria Norte"));

        var request = academyRequest("Academia Preuniversitaria Norte", null, null, null);

        mockMvc.perform(post("/api/v1/academies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "An academy with this name already exists for this organization: Academia Preuniversitaria Norte"));
    }

    // -------------------------------------------------------------------------
    // Escenario error: tipo de cuenta incorrecto → 409
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void createAcademy_whenWrongProfileType_returns409() throws Exception {
        when(academyService.createAcademy(eq("student@example.com"), any()))
                .thenThrow(new WrongProfileTypeException(
                        "Account type is not ORGANIZATION. Current type: STUDENT"));

        var request = academyRequest("Academia Norte", null, null, null);

        mockMvc.perform(post("/api/v1/academies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "Account type is not ORGANIZATION. Current type: STUDENT"));
    }

    // -------------------------------------------------------------------------
    // Escenario error: perfil base o de organización no existe → 404
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "nuevo@example.com")
    void createAcademy_whenProfileNotFound_returns404() throws Exception {
        when(academyService.createAcademy(eq("nuevo@example.com"), any()))
                .thenThrow(new ProfileNotFoundException(
                        "Profile not found for user: nuevo@example.com"));

        var request = academyRequest("Academia Norte", null, null, null);

        mockMvc.perform(post("/api/v1/academies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(
                        "Profile not found for user: nuevo@example.com"));
    }

    @Test
    @WithMockUser(username = "org@example.com")
    void createAcademy_whenOrganizationProfileNotFound_returns404() throws Exception {
        when(academyService.createAcademy(eq("org@example.com"), any()))
                .thenThrow(new ProfileNotFoundException(
                        "Organization profile not found for user: org@example.com"));

        var request = academyRequest("Academia Norte", null, null, null);

        mockMvc.perform(post("/api/v1/academies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(
                        "Organization profile not found for user: org@example.com"));
    }

    // -------------------------------------------------------------------------
    // Sin autenticación → 401
    // -------------------------------------------------------------------------

    @Test
    void createAcademy_withoutAuth_returns401() throws Exception {
        var request = academyRequest("Academia Norte", null, null, null);

        mockMvc.perform(post("/api/v1/academies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private CreateAcademyRequest academyRequest(
            String name, String description, String website, String email) {
        var r = new CreateAcademyRequest();
        r.setName(name);
        r.setDescription(description);
        r.setWebsite(website);
        r.setEmail(email);
        return r;
    }

    private AcademyResponse buildAcademyResponse(
            String name, String description, String website, String email) {
        Academy academy = Academy.builder()
                .id(UUID.randomUUID())
                .ownerProfileId(UUID.randomUUID())
                .name(name)
                .description(description)
                .website(website)
                .email(email)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return new AcademyResponse(academy);
    }
}
