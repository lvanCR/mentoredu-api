package com.mentoredu.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.profile.dto.ProfileResponse;
import com.mentoredu.profile.dto.SelectAccountTypeRequest;
import com.mentoredu.profile.exception.ProfileAlreadyExistsException;
import com.mentoredu.profile.model.Profile;
import com.mentoredu.profile.model.ProfileType;
import com.mentoredu.profile.service.IProfileService;
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

@WebMvcTest(controllers = ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IProfileService profileService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // -------------------------------------------------------------------------
    // Escenario 1 y 3 — Selección exitosa
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "juan@example.com")
    void selectAccountType_withStudent_returns201() throws Exception {
        var response = buildProfileResponse(ProfileType.STUDENT);
        when(profileService.selectAccountType(eq("juan@example.com"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/profiles/account-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestFor(ProfileType.STUDENT))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileType").value("STUDENT"))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.displayName").value("Juan Pérez"));
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void selectAccountType_withTeacher_returns201() throws Exception {
        var response = buildProfileResponse(ProfileType.TEACHER);
        when(profileService.selectAccountType(eq("teacher@example.com"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/profiles/account-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestFor(ProfileType.TEACHER))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileType").value("TEACHER"));
    }

    @Test
    @WithMockUser(username = "org@example.com")
    void selectAccountType_withOrganization_returns201() throws Exception {
        var response = buildProfileResponse(ProfileType.ORGANIZATION);
        when(profileService.selectAccountType(eq("org@example.com"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/profiles/account-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestFor(ProfileType.ORGANIZATION))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileType").value("ORGANIZATION"));
    }

    // -------------------------------------------------------------------------
    // Escenario 2 — Cuenta ya tiene perfil → 409
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "juan@example.com")
    void selectAccountType_whenProfileAlreadyExists_returns409() throws Exception {
        when(profileService.selectAccountType(any(), any()))
                .thenThrow(new ProfileAlreadyExistsException(
                        "User already has a profile. Account type cannot be changed."));

        mockMvc.perform(post("/api/v1/profiles/account-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestFor(ProfileType.STUDENT))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("User already has a profile. Account type cannot be changed."));
    }

    // -------------------------------------------------------------------------
    // Escenario 4 — Tipo no permitido → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "juan@example.com")
    void selectAccountType_withInvalidType_returns400() throws Exception {
        String invalidBody = "{\"profileType\": \"ADMIN\"}";

        mockMvc.perform(post("/api/v1/profiles/account-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @WithMockUser(username = "juan@example.com")
    void selectAccountType_withMissingProfileType_returns400() throws Exception {
        String emptyBody = "{}";

        mockMvc.perform(post("/api/v1/profiles/account-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.profileType").exists());
    }

    // -------------------------------------------------------------------------
    // Sin autenticación → 401
    // -------------------------------------------------------------------------

    @Test
    void selectAccountType_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/profiles/account-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestFor(ProfileType.STUDENT))))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private SelectAccountTypeRequest requestFor(ProfileType type) {
        var r = new SelectAccountTypeRequest();
        r.setProfileType(type);
        return r;
    }

    private ProfileResponse buildProfileResponse(ProfileType type) {
        Profile profile = Profile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .displayName("Juan Pérez")
                .profileType(type.name())
                .createdAt(LocalDateTime.now())
                .build();
        return new ProfileResponse(profile);
    }
}
