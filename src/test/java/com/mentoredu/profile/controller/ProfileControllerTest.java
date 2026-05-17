package com.mentoredu.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.profile.dto.CreateStudentProfileRequest;
import com.mentoredu.profile.dto.CreateTeacherProfileRequest;
import com.mentoredu.profile.dto.ProfileResponse;
import com.mentoredu.profile.dto.SelectAccountTypeRequest;
import com.mentoredu.profile.dto.StudentProfileResponse;
import com.mentoredu.profile.dto.TeacherProfileResponse;
import com.mentoredu.profile.dto.UpdateProfileRequest;
import com.mentoredu.profile.dto.UpdateStudentProfileRequest;
import com.mentoredu.profile.dto.UpdateTeacherProfileRequest;
import com.mentoredu.profile.exception.ProfileAlreadyExistsException;
import com.mentoredu.profile.exception.ProfileNotFoundException;
import com.mentoredu.profile.exception.StudentProfileAlreadyExistsException;
import com.mentoredu.profile.exception.TeacherProfileAlreadyExistsException;
import com.mentoredu.profile.exception.WrongProfileTypeException;
import com.mentoredu.profile.model.Profile;
import com.mentoredu.profile.model.ProfileType;
import com.mentoredu.profile.model.StudentProfile;
import com.mentoredu.profile.model.TeacherProfile;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    // =========================================================================
    // US05 — Update common profile data
    // =========================================================================

    // -------------------------------------------------------------------------
    // Escenario 1 — Actualización exitosa → 200
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "juan@example.com")
    void updateProfile_withValidData_returns200() throws Exception {
        var request = updateProfileRequest("Juan Actualizado", "Lima", "Bio actualizada");
        var response = buildUpdatedProfileResponse("Juan Actualizado", "Lima", "Bio actualizada");
        when(profileService.updateProfile(eq("juan@example.com"), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/profiles/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Juan Actualizado"))
                .andExpect(jsonPath("$.city").value("Lima"))
                .andExpect(jsonPath("$.bio").value("Bio actualizada"))
                .andExpect(jsonPath("$.profileType").value("STUDENT"));
    }

    // -------------------------------------------------------------------------
    // Escenario 1 alt — Solo nombre y ciudad → 200 (sin afectar profileType)
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "juan@example.com")
    void updateProfile_withOnlyNameAndCity_returns200() throws Exception {
        var request = updateProfileRequest("Solo Nombre", "Arequipa", null);
        var response = buildUpdatedProfileResponse("Solo Nombre", "Arequipa", null);
        when(profileService.updateProfile(eq("juan@example.com"), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/profiles/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Solo Nombre"))
                .andExpect(jsonPath("$.city").value("Arequipa"))
                .andExpect(jsonPath("$.profileType").value("STUDENT"));
    }

    // -------------------------------------------------------------------------
    // Escenario 2 — displayName vacío → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "juan@example.com")
    void updateProfile_withBlankDisplayName_returns400() throws Exception {
        var request = updateProfileRequest("", "Lima", null);

        mockMvc.perform(patch("/api/v1/profiles/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.displayName").exists());
    }

    @Test
    @WithMockUser(username = "juan@example.com")
    void updateProfile_withMissingDisplayName_returns400() throws Exception {
        String body = "{\"city\": \"Lima\"}";

        mockMvc.perform(patch("/api/v1/profiles/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.displayName").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario — Perfil no existe → 404
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "sinperfil@example.com")
    void updateProfile_whenProfileNotFound_returns404() throws Exception {
        when(profileService.updateProfile(eq("sinperfil@example.com"), any()))
                .thenThrow(new ProfileNotFoundException("Profile not found for user: sinperfil@example.com"));

        var request = updateProfileRequest("Nombre", null, null);

        mockMvc.perform(patch("/api/v1/profiles/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // -------------------------------------------------------------------------
    // Sin autenticación → 401
    // -------------------------------------------------------------------------

    @Test
    void updateProfile_withoutAuth_returns401() throws Exception {
        var request = updateProfileRequest("Juan", null, null);

        mockMvc.perform(patch("/api/v1/profiles/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

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

    private UpdateProfileRequest updateProfileRequest(String displayName, String city, String bio) {
        var r = new UpdateProfileRequest();
        r.setDisplayName(displayName);
        r.setCity(city);
        r.setBio(bio);
        return r;
    }

    private ProfileResponse buildUpdatedProfileResponse(String displayName, String city, String bio) {
        Profile profile = Profile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .displayName(displayName)
                .city(city)
                .bio(bio)
                .profileType(ProfileType.STUDENT.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return new ProfileResponse(profile);
    }

    // =========================================================================
    // US06 — Create student profile
    // =========================================================================

    // -------------------------------------------------------------------------
    // Escenario 1 — Creación exitosa con campos obligatorios → 201
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void createStudentProfile_withRequiredFields_returns201() throws Exception {
        var request = studentProfileRequest("5TO_SECUNDARIA", "Universidad Nacional Mayor de San Marcos", null, null, null);
        var response = buildStudentProfileResponse("5TO_SECUNDARIA", "Universidad Nacional Mayor de San Marcos");
        when(profileService.createStudentProfile(eq("student@example.com"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/profiles/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gradeLevel").value("5TO_SECUNDARIA"))
                .andExpect(jsonPath("$.targetUniversity").value("Universidad Nacional Mayor de San Marcos"))
                .andExpect(jsonPath("$.profileId").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario 1 alt — Creación exitosa con todos los campos → 201
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void createStudentProfile_withAllFields_returns201() throws Exception {
        var request = studentProfileRequest("4TO_SECUNDARIA", "Pontificia Universidad Católica del Perú",
                "Colegio Nacional", "Ingeniería de Sistemas", "MAÑANA");
        var response = buildStudentProfileResponse("4TO_SECUNDARIA", "Pontificia Universidad Católica del Perú");
        when(profileService.createStudentProfile(eq("student@example.com"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/profiles/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gradeLevel").value("4TO_SECUNDARIA"))
                .andExpect(jsonPath("$.targetUniversity").value("Pontificia Universidad Católica del Perú"));
    }

    // -------------------------------------------------------------------------
    // Escenario 2 — gradeLevel vacío → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void createStudentProfile_withBlankGradeLevel_returns400() throws Exception {
        var request = studentProfileRequest("", "UNMSM", null, null, null);

        mockMvc.perform(post("/api/v1/profiles/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.gradeLevel").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario 2 alt — targetUniversity vacío → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void createStudentProfile_withBlankTargetUniversity_returns400() throws Exception {
        var request = studentProfileRequest("5TO_SECUNDARIA", "", null, null, null);

        mockMvc.perform(post("/api/v1/profiles/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.targetUniversity").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario — Perfil de estudiante ya existe (RN-08) → 409
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void createStudentProfile_whenAlreadyExists_returns409() throws Exception {
        when(profileService.createStudentProfile(eq("student@example.com"), any()))
                .thenThrow(new StudentProfileAlreadyExistsException(
                        "Student profile already exists for this account."));

        var request = studentProfileRequest("5TO_SECUNDARIA", "UNMSM", null, null, null);

        mockMvc.perform(post("/api/v1/profiles/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Student profile already exists for this account."));
    }

    // -------------------------------------------------------------------------
    // Escenario — Tipo de cuenta incorrecto (no STUDENT) → 409
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createStudentProfile_whenWrongProfileType_returns409() throws Exception {
        when(profileService.createStudentProfile(eq("teacher@example.com"), any()))
                .thenThrow(new WrongProfileTypeException(
                        "Account type is not STUDENT. Current type: TEACHER"));

        var request = studentProfileRequest("5TO_SECUNDARIA", "UNMSM", null, null, null);

        mockMvc.perform(post("/api/v1/profiles/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    // -------------------------------------------------------------------------
    // Escenario — Sin perfil base (US04 no ejecutada) → 404
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "nuevo@example.com")
    void createStudentProfile_whenBaseProfileNotFound_returns404() throws Exception {
        when(profileService.createStudentProfile(eq("nuevo@example.com"), any()))
                .thenThrow(new ProfileNotFoundException("Profile not found for user: nuevo@example.com"));

        var request = studentProfileRequest("5TO_SECUNDARIA", "UNMSM", null, null, null);

        mockMvc.perform(post("/api/v1/profiles/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // -------------------------------------------------------------------------
    // Sin autenticación → 401
    // -------------------------------------------------------------------------

    @Test
    void createStudentProfile_withoutAuth_returns401() throws Exception {
        var request = studentProfileRequest("5TO_SECUNDARIA", "UNMSM", null, null, null);

        mockMvc.perform(post("/api/v1/profiles/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    private CreateStudentProfileRequest studentProfileRequest(
            String gradeLevel, String targetUniversity,
            String schoolName, String targetCareer, String studyShift) {
        var r = new CreateStudentProfileRequest();
        r.setGradeLevel(gradeLevel);
        r.setTargetUniversity(targetUniversity);
        r.setSchoolName(schoolName);
        r.setTargetCareer(targetCareer);
        r.setStudyShift(studyShift);
        return r;
    }

    private StudentProfileResponse buildStudentProfileResponse(String gradeLevel, String targetUniversity) {
        StudentProfile sp = StudentProfile.builder()
                .profileId(UUID.randomUUID())
                .gradeLevel(gradeLevel)
                .targetUniversity(targetUniversity)
                .build();
        return new StudentProfileResponse(sp);
    }

    // =========================================================================
    // US07 — Update student target university
    // =========================================================================

    // -------------------------------------------------------------------------
    // Escenario 1 — Actualización exitosa → 200
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void updateStudentProfile_withValidTargetUniversity_returns200() throws Exception {
        var request = updateStudentProfileRequest("Universidad Nacional Mayor de San Marcos");
        var response = buildStudentProfileResponseFull("5TO_SECUNDARIA",
                "Universidad Nacional Mayor de San Marcos", "Colegio Nacional", "Ingeniería", "MAÑANA");
        when(profileService.updateStudentProfile(eq("student@example.com"), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/profiles/student/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetUniversity").value("Universidad Nacional Mayor de San Marcos"))
                .andExpect(jsonPath("$.profileId").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario 3 — Solo targetUniversity; demás campos sin cambios → 200
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void updateStudentProfile_withOnlyTargetUniversity_returns200() throws Exception {
        var request = updateStudentProfileRequest("Pontificia Universidad Católica del Perú");
        var response = buildStudentProfileResponseFull("5TO_SECUNDARIA",
                "Pontificia Universidad Católica del Perú", "Colegio Nacional", "Ingeniería", "MAÑANA");
        when(profileService.updateStudentProfile(eq("student@example.com"), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/profiles/student/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetUniversity").value("Pontificia Universidad Católica del Perú"))
                .andExpect(jsonPath("$.gradeLevel").value("5TO_SECUNDARIA"))
                .andExpect(jsonPath("$.schoolName").value("Colegio Nacional"));
    }

    // -------------------------------------------------------------------------
    // Escenario 2 — targetUniversity vacío → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void updateStudentProfile_withBlankTargetUniversity_returns400() throws Exception {
        var request = updateStudentProfileRequest("");

        mockMvc.perform(patch("/api/v1/profiles/student/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.targetUniversity").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario 4 — Perfil de estudiante no existe → 404
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void updateStudentProfile_whenStudentProfileNotFound_returns404() throws Exception {
        when(profileService.updateStudentProfile(eq("student@example.com"), any()))
                .thenThrow(new ProfileNotFoundException(
                        "Student profile not found for user: student@example.com"));

        var request = updateStudentProfileRequest("UNMSM");

        mockMvc.perform(patch("/api/v1/profiles/student/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Student profile not found for user: student@example.com"));
    }

    // -------------------------------------------------------------------------
    // Sin autenticación → 401
    // -------------------------------------------------------------------------

    @Test
    void updateStudentProfile_withoutAuth_returns401() throws Exception {
        var request = updateStudentProfileRequest("UNMSM");

        mockMvc.perform(patch("/api/v1/profiles/student/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers US07
    // =========================================================================

    private UpdateStudentProfileRequest updateStudentProfileRequest(String targetUniversity) {
        var r = new UpdateStudentProfileRequest();
        r.setTargetUniversity(targetUniversity);
        return r;
    }

    private StudentProfileResponse buildStudentProfileResponseFull(
            String gradeLevel, String targetUniversity,
            String schoolName, String targetCareer, String studyShift) {
        StudentProfile sp = StudentProfile.builder()
                .profileId(UUID.randomUUID())
                .gradeLevel(gradeLevel)
                .targetUniversity(targetUniversity)
                .schoolName(schoolName)
                .targetCareer(targetCareer)
                .studyShift(studyShift)
                .build();
        return new StudentProfileResponse(sp);
    }

    // =========================================================================
    // US08 — Create teacher profile
    // =========================================================================

    // -------------------------------------------------------------------------
    // Escenario 1 — Creación exitosa con campos obligatorios → 201
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createTeacherProfile_withRequiredFields_returns201() throws Exception {
        var request = teacherProfileRequest("Matemáticas", "Instituto Preuniversitario El Triunfo", null);
        var response = buildTeacherProfileResponse("Matemáticas", "Instituto Preuniversitario El Triunfo", null);
        when(profileService.createTeacherProfile(eq("teacher@example.com"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/profiles/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.specialty").value("Matemáticas"))
                .andExpect(jsonPath("$.institutionName").value("Instituto Preuniversitario El Triunfo"))
                .andExpect(jsonPath("$.profileId").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario 3 — Creación exitosa con todos los campos → 201
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createTeacherProfile_withAllFields_returns201() throws Exception {
        var request = teacherProfileRequest("Física", "Academia Preuniversitaria Lumbre", "Docente con 10 años de experiencia.");
        var response = buildTeacherProfileResponse("Física", "Academia Preuniversitaria Lumbre", "Docente con 10 años de experiencia.");
        when(profileService.createTeacherProfile(eq("teacher@example.com"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/profiles/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.specialty").value("Física"))
                .andExpect(jsonPath("$.institutionName").value("Academia Preuniversitaria Lumbre"))
                .andExpect(jsonPath("$.bioProfessional").value("Docente con 10 años de experiencia."));
    }

    // -------------------------------------------------------------------------
    // Escenario 2 — specialty vacío → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createTeacherProfile_withBlankSpecialty_returns400() throws Exception {
        var request = teacherProfileRequest("", "Academia Lumbre", null);

        mockMvc.perform(post("/api/v1/profiles/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.specialty").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario 2 alt — institutionName vacío → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createTeacherProfile_withBlankInstitutionName_returns400() throws Exception {
        var request = teacherProfileRequest("Química", "", null);

        mockMvc.perform(post("/api/v1/profiles/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.institutionName").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario 4 — Especialidad no válida (specialty nulo → falta el campo) → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createTeacherProfile_withMissingSpecialty_returns400() throws Exception {
        String body = "{\"institutionName\": \"Academia Lumbre\"}";

        mockMvc.perform(post("/api/v1/profiles/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.specialty").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario — Tipo de cuenta incorrecto (no TEACHER) → 409
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "student@example.com")
    void createTeacherProfile_whenWrongProfileType_returns409() throws Exception {
        when(profileService.createTeacherProfile(eq("student@example.com"), any()))
                .thenThrow(new WrongProfileTypeException(
                        "Account type is not TEACHER. Current type: STUDENT"));

        var request = teacherProfileRequest("Matemáticas", "Academia Lumbre", null);

        mockMvc.perform(post("/api/v1/profiles/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Account type is not TEACHER. Current type: STUDENT"));
    }

    // -------------------------------------------------------------------------
    // Escenario — Perfil de docente ya existe (RN-09) → 409
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createTeacherProfile_whenAlreadyExists_returns409() throws Exception {
        when(profileService.createTeacherProfile(eq("teacher@example.com"), any()))
                .thenThrow(new TeacherProfileAlreadyExistsException(
                        "Teacher profile already exists for this account."));

        var request = teacherProfileRequest("Matemáticas", "Academia Lumbre", null);

        mockMvc.perform(post("/api/v1/profiles/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Teacher profile already exists for this account."));
    }

    // -------------------------------------------------------------------------
    // Escenario — Perfil base no existe (US04 no ejecutada) → 404
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "nuevo@example.com")
    void createTeacherProfile_whenBaseProfileNotFound_returns404() throws Exception {
        when(profileService.createTeacherProfile(eq("nuevo@example.com"), any()))
                .thenThrow(new ProfileNotFoundException("Profile not found for user: nuevo@example.com"));

        var request = teacherProfileRequest("Química", "Academia Lumbre", null);

        mockMvc.perform(post("/api/v1/profiles/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Profile not found for user: nuevo@example.com"));
    }

    // -------------------------------------------------------------------------
    // Sin autenticación → 401
    // -------------------------------------------------------------------------

    @Test
    void createTeacherProfile_withoutAuth_returns401() throws Exception {
        var request = teacherProfileRequest("Matemáticas", "Academia Lumbre", null);

        mockMvc.perform(post("/api/v1/profiles/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers US08
    // =========================================================================

    private CreateTeacherProfileRequest teacherProfileRequest(
            String specialty, String institutionName, String bioProfessional) {
        var r = new CreateTeacherProfileRequest();
        r.setSpecialty(specialty);
        r.setInstitutionName(institutionName);
        r.setBioProfessional(bioProfessional);
        return r;
    }

    private TeacherProfileResponse buildTeacherProfileResponse(
            String specialty, String institutionName, String bioProfessional) {
        TeacherProfile tp = TeacherProfile.builder()
                .profileId(UUID.randomUUID())
                .specialty(specialty)
                .institutionName(institutionName)
                .bioProfessional(bioProfessional)
                .build();
        return new TeacherProfileResponse(tp);
    }

    // =========================================================================
    // US09 — Update teacher specialty
    // =========================================================================

    // -------------------------------------------------------------------------
    // Escenario 1 — Actualización exitosa → 200
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "teacher@example.com")
    void updateTeacherProfile_withValidSpecialty_returns200() throws Exception {
        var request = updateTeacherProfileRequest("Química", "Instituto Preuniversitario El Triunfo", null);
        var response = buildTeacherProfileResponse("Química", "Instituto Preuniversitario El Triunfo", null);
        when(profileService.updateTeacherProfile(eq("teacher@example.com"), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/profiles/teacher/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty").value("Química"))
                .andExpect(jsonPath("$.institutionName").value("Instituto Preuniversitario El Triunfo"))
                .andExpect(jsonPath("$.profileId").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario 3 — Solo specialty; demás campos sin cambios → 200
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "teacher@example.com")
    void updateTeacherProfile_withOnlySpecialty_returns200() throws Exception {
        var request = updateTeacherProfileRequest("Física", null, null);
        var response = buildTeacherProfileResponse("Física", "Academia Lumbre", "Bio previa del docente.");
        when(profileService.updateTeacherProfile(eq("teacher@example.com"), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/profiles/teacher/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty").value("Física"))
                .andExpect(jsonPath("$.institutionName").value("Academia Lumbre"))
                .andExpect(jsonPath("$.bioProfessional").value("Bio previa del docente."));
    }

    // -------------------------------------------------------------------------
    // Escenario 2 — specialty vacía → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "teacher@example.com")
    void updateTeacherProfile_withBlankSpecialty_returns400() throws Exception {
        var request = updateTeacherProfileRequest("", null, null);

        mockMvc.perform(patch("/api/v1/profiles/teacher/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.specialty").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario 4 — Perfil de docente no existe → 404
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "teacher@example.com")
    void updateTeacherProfile_whenTeacherProfileNotFound_returns404() throws Exception {
        when(profileService.updateTeacherProfile(eq("teacher@example.com"), any()))
                .thenThrow(new ProfileNotFoundException(
                        "Teacher profile not found for user: teacher@example.com"));

        var request = updateTeacherProfileRequest("Biología", null, null);

        mockMvc.perform(patch("/api/v1/profiles/teacher/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Teacher profile not found for user: teacher@example.com"));
    }

    // -------------------------------------------------------------------------
    // Sin autenticación → 401
    // -------------------------------------------------------------------------

    @Test
    void updateTeacherProfile_withoutAuth_returns401() throws Exception {
        var request = updateTeacherProfileRequest("Matemáticas", null, null);

        mockMvc.perform(patch("/api/v1/profiles/teacher/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers US09
    // =========================================================================

    private UpdateTeacherProfileRequest updateTeacherProfileRequest(
            String specialty, String institutionName, String bioProfessional) {
        var r = new UpdateTeacherProfileRequest();
        r.setSpecialty(specialty);
        r.setInstitutionName(institutionName);
        r.setBioProfessional(bioProfessional);
        return r;
    }
}
