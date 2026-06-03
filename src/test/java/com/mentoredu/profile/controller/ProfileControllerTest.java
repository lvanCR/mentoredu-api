package com.mentoredu.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.profile.dto.*;
import com.mentoredu.profile.exception.*;
import com.mentoredu.profile.service.IProfileService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de capa web para ProfileController.
 * Cubre: US04 (perfil estudiante), US05 (perfil docente), US06 (perfil academia),
 *        GET /profiles/me y GET /profiles/{userId}.
 * Reglas: RN-01 (rol único), WrongProfileTypeException → 403.
 * IMPORTANTE: Para tests sin autenticación (@WithMockUser ausente), el body DEBE ser
 * válido para que la validación Bean pase y se llegue a SecurityUtils (que lanza 401).
 * Si el body es inválido, Bean Validation retorna 400 ANTES de llegar a SecurityUtils.
 */
@WebMvcTest(
    controllers = ProfileController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class ProfileControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean private IProfileService profileService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private UserDetailsService userDetailsService;

    // ── GET /profiles/me ───────────────────────────────────────────────────────

    /** GET /profiles/me — usuario autenticado → 200 */
    @Test
    @WithMockUser(username = "user@example.com")
    void getMyProfile_authenticated_returns200() throws Exception {
        var response = Mockito.mock(ProfileMeResponse.class);
        when(profileService.getMyProfile("user@example.com")).thenReturn(response);

        mockMvc.perform(get("/api/v1/profiles/me"))
            .andExpect(status().isOk());
    }

    /** GET /profiles/me — sin autenticación → 401 */
    @Test
    void getMyProfile_unauthenticated_returns401() throws Exception {
        // GET no tiene @RequestBody → SecurityUtils.currentEmail() se llama directamente
        mockMvc.perform(get("/api/v1/profiles/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    /** GET /profiles/me — perfil no creado → 404 */
    @Test
    @WithMockUser(username = "user@example.com")
    void getMyProfile_profileNotFound_returns404() throws Exception {
        when(profileService.getMyProfile(any()))
            .thenThrow(new ProfileNotFoundException("Profile not found"));

        mockMvc.perform(get("/api/v1/profiles/me"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ── PATCH /profiles/me ─────────────────────────────────────────────────────

    /** US04/US05/US06 PATCH /profiles/me — actualizar displayName → 200 */
    @Test
    @WithMockUser(username = "user@example.com")
    void updateProfile_withValidDisplayName_returns200() throws Exception {
        var response = Mockito.mock(ProfileResponse.class);
        when(profileService.updateProfile(eq("user@example.com"), any())).thenReturn(response);

        // UpdateProfileRequest requiere @NotBlank displayName
        var req = new UpdateProfileRequest();
        req.setDisplayName("Juan Pérez");

        mockMvc.perform(patch("/api/v1/profiles/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk());
    }

    /** PATCH /profiles/me — displayName en blanco → 400 (validación) */
    @Test
    @WithMockUser(username = "user@example.com")
    void updateProfile_blankDisplayName_returns400() throws Exception {
        var req = new UpdateProfileRequest();
        req.setDisplayName("");

        mockMvc.perform(patch("/api/v1/profiles/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.displayName").exists());
    }

    /** PATCH /profiles/me — sin autenticación + body válido → 401 */
    @Test
    void updateProfile_unauthenticated_returns401() throws Exception {
        // Necesario enviar body válido para que @Valid pase y llegue a SecurityUtils
        var req = new UpdateProfileRequest();
        req.setDisplayName("Juan");

        mockMvc.perform(patch("/api/v1/profiles/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    /** PATCH /profiles/me — perfil no encontrado → 404 */
    @Test
    @WithMockUser(username = "user@example.com")
    void updateProfile_profileNotFound_returns404() throws Exception {
        when(profileService.updateProfile(any(), any()))
            .thenThrow(new ProfileNotFoundException("Profile not found"));

        var req = new UpdateProfileRequest();
        req.setDisplayName("Juan");

        mockMvc.perform(patch("/api/v1/profiles/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isNotFound());
    }

    // ── POST /profiles/student (US04) ──────────────────────────────────────────

    /** US04 Escenario 4.1 — STUDENT crea perfil con gradeLevel mínimo → 201 */
    @Test
    @WithMockUser(username = "student@example.com")
    void createStudentProfile_withGradeLevel_returns201() throws Exception {
        var response = Mockito.mock(StudentProfileResponse.class);
        when(profileService.createStudentProfile(eq("student@example.com"), any())).thenReturn(response);

        // gradeLevel es @NotBlank — debe proveer un valor
        var req = new CreateStudentProfileRequest();
        req.setGradeLevel("5to secundaria");

        mockMvc.perform(post("/api/v1/profiles/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
    }

    /** US04 Escenario 4.3 — gradeLevel vacío → 400 (validación @NotBlank) */
    @Test
    @WithMockUser(username = "student@example.com")
    void createStudentProfile_blankGradeLevel_returns400() throws Exception {
        var req = new CreateStudentProfileRequest();
        req.setGradeLevel("");

        mockMvc.perform(post("/api/v1/profiles/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.gradeLevel").exists());
    }

    /** US04 — perfil de estudiante ya existe → 409 Conflict */
    @Test
    @WithMockUser(username = "student@example.com")
    void createStudentProfile_alreadyExists_returns409() throws Exception {
        when(profileService.createStudentProfile(any(), any()))
            .thenThrow(new StudentProfileAlreadyExistsException("Student profile already exists"));

        var req = new CreateStudentProfileRequest();
        req.setGradeLevel("4to secundaria");

        mockMvc.perform(post("/api/v1/profiles/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Conflict"));
    }

    /** US04 / RN-01 Escenario 4.6 — TEACHER intenta crear perfil STUDENT → 403 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void createStudentProfile_wrongRole_returns403() throws Exception {
        when(profileService.createStudentProfile(any(), any()))
            .thenThrow(new WrongProfileTypeException("Only STUDENT users can create a student profile"));

        var req = new CreateStudentProfileRequest();
        req.setGradeLevel("4to secundaria");

        mockMvc.perform(post("/api/v1/profiles/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    /** US04 Escenario 4.7 — sin autenticación + body válido → 401 */
    @Test
    void createStudentProfile_unauthenticated_returns401() throws Exception {
        var req = new CreateStudentProfileRequest();
        req.setGradeLevel("5to secundaria");

        mockMvc.perform(post("/api/v1/profiles/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /profiles/student/{userId} (US04) ─────────────────────────────────

    /** US04 Escenario 4.9 — perfil de estudiante por userId → 200 */
    @Test
    @WithMockUser(username = "user@example.com")
    void getStudentProfile_existingUser_returns200() throws Exception {
        var userId = UUID.randomUUID();
        var response = Mockito.mock(StudentProfileResponse.class);
        when(profileService.getStudentProfile(userId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/profiles/student/" + userId))
            .andExpect(status().isOk());
    }

    /** US04 — perfil no encontrado → 404 */
    @Test
    @WithMockUser(username = "user@example.com")
    void getStudentProfile_notFound_returns404() throws Exception {
        when(profileService.getStudentProfile(any()))
            .thenThrow(new ProfileNotFoundException("Student profile not found"));

        mockMvc.perform(get("/api/v1/profiles/student/" + UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    // ── PATCH /profiles/student/me (US04 Escenario 4.8) ──────────────────────

    /** US04 Escenario 4.8 — actualizar targetUniversityId → 200 */
    @Test
    @WithMockUser(username = "student@example.com")
    void updateStudentProfile_withValidData_returns200() throws Exception {
        var response = Mockito.mock(StudentProfileResponse.class);
        when(profileService.updateStudentProfile(eq("student@example.com"), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/profiles/student/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUniversityId\":\"b1000000-0000-0000-0000-000000000001\"}"))
            .andExpect(status().isOk());
    }

    /** PATCH /profiles/student/me — sin autenticación → 401 */
    @Test
    void updateStudentProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/profiles/student/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    // ── POST /profiles/teacher (US05) ─────────────────────────────────────────

    /** US05 Escenario 5.1 — TEACHER crea perfil → 201 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void createTeacherProfile_withValidData_returns201() throws Exception {
        var response = Mockito.mock(TeacherProfileResponse.class);
        when(profileService.createTeacherProfile(eq("teacher@example.com"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/profiles/teacher")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")) // bio es opcional
            .andExpect(status().isCreated());
    }

    /** US05 Escenario 5.4 — perfil de docente ya existe → 409 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void createTeacherProfile_alreadyExists_returns409() throws Exception {
        when(profileService.createTeacherProfile(any(), any()))
            .thenThrow(new TeacherProfileAlreadyExistsException("Teacher profile already exists"));

        mockMvc.perform(post("/api/v1/profiles/teacher")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isConflict());
    }

    /** US05 Escenario 5.5 / RN-01 — STUDENT intenta crear perfil TEACHER → 403 */
    @Test
    @WithMockUser(username = "student@example.com")
    void createTeacherProfile_wrongRole_returns403() throws Exception {
        when(profileService.createTeacherProfile(any(), any()))
            .thenThrow(new WrongProfileTypeException("Only TEACHER users can create a teacher profile"));

        mockMvc.perform(post("/api/v1/profiles/teacher")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }

    /** POST /profiles/teacher — sin autenticación → 401 */
    @Test
    void createTeacherProfile_unauthenticated_returns401() throws Exception {
        // CreateTeacherProfileRequest no tiene campos @NotBlank → body vacío pasa validación
        mockMvc.perform(post("/api/v1/profiles/teacher")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /profiles/teacher/me (US05) ────────────────────────────────────────

    /** US05 — obtener mi perfil de docente → 200 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void getMyTeacherProfile_authenticated_returns200() throws Exception {
        var response = Mockito.mock(TeacherProfileResponse.class);
        when(profileService.getTeacherProfile("teacher@example.com")).thenReturn(response);

        mockMvc.perform(get("/api/v1/profiles/teacher/me"))
            .andExpect(status().isOk());
    }

    /** GET /profiles/teacher/me — sin autenticación → 401 */
    @Test
    void getMyTeacherProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/profiles/teacher/me"))
            .andExpect(status().isUnauthorized());
    }

    // ── PATCH /profiles/teacher/me (US05 Escenario 5.6) ──────────────────────

    /** US05 Escenario 5.6 — actualizar bio del docente → 200 */
    @Test
    @WithMockUser(username = "teacher@example.com")
    void updateTeacherProfile_bio_returns200() throws Exception {
        var response = Mockito.mock(TeacherProfileResponse.class);
        when(profileService.updateTeacherProfile(eq("teacher@example.com"), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/profiles/teacher/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bioProfessional\":\"Profesor de Matemáticas\"}"))
            .andExpect(status().isOk());
    }

    /** PATCH /profiles/teacher/me — sin autenticación → 401 */
    @Test
    void updateTeacherProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/profiles/teacher/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    // ── POST /profiles/academy (US06) ─────────────────────────────────────────

    /** US06 Escenario 6.1 — ACADEMY crea perfil con academyName → 201 */
    @Test
    @WithMockUser(username = "academy@example.com")
    void createAcademyProfile_withAcademyName_returns201() throws Exception {
        var response = Mockito.mock(AcademyProfileResponse.class);
        when(profileService.createAcademyProfile(eq("academy@example.com"), any())).thenReturn(response);

        // CreateAcademyProfileRequest es un record con @NotBlank academyName
        var req = new CreateAcademyProfileRequest("Academia Mendel", null, null, null);

        mockMvc.perform(post("/api/v1/profiles/academy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
    }

    /** US06 Escenario 6.2 — academyName omitido → 400 (validación @NotBlank) */
    @Test
    @WithMockUser(username = "academy@example.com")
    void createAcademyProfile_blankAcademyName_returns400() throws Exception {
        var req = new CreateAcademyProfileRequest("", null, null, null);

        mockMvc.perform(post("/api/v1/profiles/academy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.academyName").exists());
    }

    /** US06 Escenario 6.3 — nombre de academia duplicado → 409 */
    @Test
    @WithMockUser(username = "academy@example.com")
    void createAcademyProfile_duplicateName_returns409() throws Exception {
        when(profileService.createAcademyProfile(any(), any()))
            .thenThrow(new AcademyNameAlreadyExistsException("Academy name already taken"));

        var req = new CreateAcademyProfileRequest("Academia Duplicada", null, null, null);

        mockMvc.perform(post("/api/v1/profiles/academy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isConflict());
    }

    /** US06 Escenario 6.4 — ya existe perfil de academia → 409 */
    @Test
    @WithMockUser(username = "academy@example.com")
    void createAcademyProfile_alreadyExists_returns409() throws Exception {
        when(profileService.createAcademyProfile(any(), any()))
            .thenThrow(new AcademyProfileAlreadyExistsException("Academy profile already exists"));

        var req = new CreateAcademyProfileRequest("Otra Academia", null, null, null);

        mockMvc.perform(post("/api/v1/profiles/academy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isConflict());
    }

    /** US06 Escenario 6.5 / RN-01 — STUDENT intenta crear perfil ACADEMY → 403 */
    @Test
    @WithMockUser(username = "student@example.com")
    void createAcademyProfile_wrongRole_returns403() throws Exception {
        when(profileService.createAcademyProfile(any(), any()))
            .thenThrow(new WrongProfileTypeException("Only ACADEMY users can create an academy profile"));

        var req = new CreateAcademyProfileRequest("Academia Test", null, null, null);

        mockMvc.perform(post("/api/v1/profiles/academy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }

    /** POST /profiles/academy — sin autenticación + body válido → 401 */
    @Test
    void createAcademyProfile_unauthenticated_returns401() throws Exception {
        // Enviar body válido para que @Valid pase y llegue a SecurityUtils
        var req = new CreateAcademyProfileRequest("Academia Mendel", null, null, null);

        mockMvc.perform(post("/api/v1/profiles/academy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /profiles/academy/me (US06) ────────────────────────────────────────

    /** US06 — obtener mi perfil de academia → 200 */
    @Test
    @WithMockUser(username = "academy@example.com")
    void getMyAcademyProfile_authenticated_returns200() throws Exception {
        var response = Mockito.mock(AcademyProfileResponse.class);
        when(profileService.getAcademyProfile("academy@example.com")).thenReturn(response);

        mockMvc.perform(get("/api/v1/profiles/academy/me"))
            .andExpect(status().isOk());
    }

    /** GET /profiles/academy/me — sin autenticación → 401 */
    @Test
    void getMyAcademyProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/profiles/academy/me"))
            .andExpect(status().isUnauthorized());
    }

    // ── PATCH /profiles/academy/me (US06 Escenario 6.6) ─────────────────────

    /** US06 Escenario 6.6 — actualizar website → 200 */
    @Test
    @WithMockUser(username = "academy@example.com")
    void updateAcademyProfile_website_returns200() throws Exception {
        var response = Mockito.mock(AcademyProfileResponse.class);
        when(profileService.updateAcademyProfile(eq("academy@example.com"), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/profiles/academy/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"website\":\"https://academia-mendel.pe\"}"))
            .andExpect(status().isOk());
    }

    /** PATCH /profiles/academy/me — sin autenticación → 401 */
    @Test
    void updateAcademyProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/profiles/academy/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /profiles/{userId} (US06 Escenario 6.7/6.8) ─────────────────────

    /** US06 Escenario 6.7 — perfil público de cualquier usuario → 200 */
    @Test
    @WithMockUser(username = "user@example.com")
    void getPublicProfile_existingUser_returns200() throws Exception {
        var userId = UUID.randomUUID();
        var response = Mockito.mock(ProfileResponse.class);
        when(profileService.getPublicProfile(eq(userId), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/profiles/" + userId))
            .andExpect(status().isOk());
    }

    /** US06 Escenario 6.8 — userId inexistente → 404 */
    @Test
    @WithMockUser(username = "user@example.com")
    void getPublicProfile_notFound_returns404() throws Exception {
        when(profileService.getPublicProfile(any(), any()))
            .thenThrow(new ProfileNotFoundException("Profile not found"));

        mockMvc.perform(get("/api/v1/profiles/" + UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    /** GET /profiles/{userId} — sin autenticación → 401 */
    @Test
    void getPublicProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/profiles/" + UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }
}
