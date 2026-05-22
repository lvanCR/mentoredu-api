package com.mentoredu.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.profile.dto.*;
import com.mentoredu.profile.exception.*;
import com.mentoredu.profile.model.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    // =========================================================================
    // US04 — Select account type
    // =========================================================================

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
    @WithMockUser(username = "academy@example.com")
    void selectAccountType_withAcademy_returns201() throws Exception {
        var response = buildProfileResponse(ProfileType.ACADEMY);
        when(profileService.selectAccountType(eq("academy@example.com"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/profiles/account-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestFor(ProfileType.ACADEMY))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileType").value("ACADEMY"));
    }

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
        mockMvc.perform(post("/api/v1/profiles/account-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.profileType").exists());
    }

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

    @Test
    void updateProfile_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/profiles/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProfileRequest("Juan", null, null))))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US06 — Create student profile
    // =========================================================================

    @Test
    @WithMockUser(username = "student@example.com")
    void createStudentProfile_withRequiredFields_returns201() throws Exception {
        var request = new CreateStudentProfileRequest();
        request.setGradeLevel("5TO_SECUNDARIA");
        var response = buildStudentProfileResponse("5TO_SECUNDARIA");
        when(profileService.createStudentProfile(eq("student@example.com"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/profiles/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gradeLevel").value("5TO_SECUNDARIA"))
                .andExpect(jsonPath("$.profileId").exists());
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void createStudentProfile_withAllFields_returns201() throws Exception {
        UUID univId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        var request = new CreateStudentProfileRequest();
        request.setGradeLevel("4TO_SECUNDARIA");
        request.setSchoolName("Colegio Nacional");
        request.setStudyShift("MAÑANA");
        request.setTargetUniversityId(univId);
        request.setTargetAreaId(areaId);
        request.setTargetCareerId(careerId);
        var response = buildStudentProfileResponseFull("4TO_SECUNDARIA", "Colegio Nacional", "MAÑANA",
                univId, areaId, careerId);
        when(profileService.createStudentProfile(eq("student@example.com"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/profiles/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gradeLevel").value("4TO_SECUNDARIA"))
                .andExpect(jsonPath("$.targetUniversityId").value(univId.toString()));
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void createStudentProfile_withBlankGradeLevel_returns400() throws Exception {
        var request = new CreateStudentProfileRequest();
        request.setGradeLevel("");

        mockMvc.perform(post("/api/v1/profiles/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.gradeLevel").exists());
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void createStudentProfile_whenAlreadyExists_returns409() throws Exception {
        when(profileService.createStudentProfile(eq("student@example.com"), any()))
                .thenThrow(new StudentProfileAlreadyExistsException(
                        "Student profile already exists for this account."));

        var request = new CreateStudentProfileRequest();
        request.setGradeLevel("5TO_SECUNDARIA");

        mockMvc.perform(post("/api/v1/profiles/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Student profile already exists for this account."));
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createStudentProfile_whenWrongProfileType_returns409() throws Exception {
        when(profileService.createStudentProfile(eq("teacher@example.com"), any()))
                .thenThrow(new WrongProfileTypeException(
                        "Account type is not STUDENT. Current type: TEACHER"));

        var request = new CreateStudentProfileRequest();
        request.setGradeLevel("5TO_SECUNDARIA");

        mockMvc.perform(post("/api/v1/profiles/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @WithMockUser(username = "nuevo@example.com")
    void createStudentProfile_whenBaseProfileNotFound_returns404() throws Exception {
        when(profileService.createStudentProfile(eq("nuevo@example.com"), any()))
                .thenThrow(new ProfileNotFoundException("Profile not found for user: nuevo@example.com"));

        var request = new CreateStudentProfileRequest();
        request.setGradeLevel("5TO_SECUNDARIA");

        mockMvc.perform(post("/api/v1/profiles/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void createStudentProfile_withoutAuth_returns401() throws Exception {
        var request = new CreateStudentProfileRequest();
        request.setGradeLevel("5TO_SECUNDARIA");

        mockMvc.perform(post("/api/v1/profiles/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Update student profile
    // =========================================================================

    @Test
    @WithMockUser(username = "student@example.com")
    void updateStudentProfile_withValidFields_returns200() throws Exception {
        UUID univId = UUID.randomUUID();
        var request = new UpdateStudentProfileRequest();
        request.setTargetUniversityId(univId);
        var response = buildStudentProfileResponseFull("5TO_SECUNDARIA", null, null, univId, null, null);
        when(profileService.updateStudentProfile(eq("student@example.com"), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/profiles/student/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetUniversityId").value(univId.toString()))
                .andExpect(jsonPath("$.profileId").exists());
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void updateStudentProfile_whenStudentProfileNotFound_returns404() throws Exception {
        when(profileService.updateStudentProfile(eq("student@example.com"), any()))
                .thenThrow(new ProfileNotFoundException(
                        "Student profile not found for user: student@example.com"));

        mockMvc.perform(patch("/api/v1/profiles/student/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void updateStudentProfile_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/profiles/student/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Create teacher profile
    // =========================================================================

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createTeacherProfile_withBioProfessional_returns201() throws Exception {
        var request = new CreateTeacherProfileRequest();
        request.setBioProfessional("Docente con 10 años de experiencia.");
        var response = buildTeacherProfileResponse("Docente con 10 años de experiencia.");
        when(profileService.createTeacherProfile(eq("teacher@example.com"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/profiles/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bioProfessional").value("Docente con 10 años de experiencia."))
                .andExpect(jsonPath("$.profileId").exists());
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createTeacherProfile_withNoFields_returns201() throws Exception {
        var response = buildTeacherProfileResponse(null);
        when(profileService.createTeacherProfile(eq("teacher@example.com"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/profiles/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileId").exists());
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void createTeacherProfile_whenWrongProfileType_returns409() throws Exception {
        when(profileService.createTeacherProfile(eq("student@example.com"), any()))
                .thenThrow(new WrongProfileTypeException(
                        "Account type is not TEACHER. Current type: STUDENT"));

        mockMvc.perform(post("/api/v1/profiles/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Account type is not TEACHER. Current type: STUDENT"));
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void createTeacherProfile_whenAlreadyExists_returns409() throws Exception {
        when(profileService.createTeacherProfile(eq("teacher@example.com"), any()))
                .thenThrow(new TeacherProfileAlreadyExistsException(
                        "Teacher profile already exists for this account."));

        mockMvc.perform(post("/api/v1/profiles/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Teacher profile already exists for this account."));
    }

    @Test
    @WithMockUser(username = "nuevo@example.com")
    void createTeacherProfile_whenBaseProfileNotFound_returns404() throws Exception {
        when(profileService.createTeacherProfile(eq("nuevo@example.com"), any()))
                .thenThrow(new ProfileNotFoundException("Profile not found for user: nuevo@example.com"));

        mockMvc.perform(post("/api/v1/profiles/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void createTeacherProfile_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/profiles/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Update teacher profile
    // =========================================================================

    @Test
    @WithMockUser(username = "teacher@example.com")
    void updateTeacherProfile_withBioProfessional_returns200() throws Exception {
        var request = new UpdateTeacherProfileRequest();
        request.setBioProfessional("Bio actualizada.");
        var response = buildTeacherProfileResponse("Bio actualizada.");
        when(profileService.updateTeacherProfile(eq("teacher@example.com"), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/profiles/teacher/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bioProfessional").value("Bio actualizada."))
                .andExpect(jsonPath("$.profileId").exists());
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void updateTeacherProfile_whenTeacherProfileNotFound_returns404() throws Exception {
        when(profileService.updateTeacherProfile(eq("teacher@example.com"), any()))
                .thenThrow(new ProfileNotFoundException(
                        "Teacher profile not found for user: teacher@example.com"));

        mockMvc.perform(patch("/api/v1/profiles/teacher/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void updateTeacherProfile_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/profiles/teacher/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Create academy profile
    // =========================================================================

    @Test
    @WithMockUser(username = "academy@example.com")
    void createAcademyProfile_withRequiredFields_returns201() throws Exception {
        var request = new CreateAcademyProfileRequest("Academia Preuniversitaria Lima", null, null, null);
        var response = buildAcademyProfileResponse("Academia Preuniversitaria Lima", null, null, null);
        when(profileService.createAcademyProfile(eq("academy@example.com"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/profiles/academy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.academyName").value("Academia Preuniversitaria Lima"))
                .andExpect(jsonPath("$.profileId").exists());
    }

    @Test
    @WithMockUser(username = "academy@example.com")
    void createAcademyProfile_withAllFields_returns201() throws Exception {
        var request = new CreateAcademyProfileRequest(
                "Academia Preuniversitaria Lima", "20123456789",
                "https://academia-lima.pe", "contacto@academia-lima.pe");
        var response = buildAcademyProfileResponse(
                "Academia Preuniversitaria Lima", "20123456789",
                "https://academia-lima.pe", "contacto@academia-lima.pe");
        when(profileService.createAcademyProfile(eq("academy@example.com"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/profiles/academy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.academyName").value("Academia Preuniversitaria Lima"))
                .andExpect(jsonPath("$.ruc").value("20123456789"))
                .andExpect(jsonPath("$.website").value("https://academia-lima.pe"))
                .andExpect(jsonPath("$.contactEmail").value("contacto@academia-lima.pe"));
    }

    @Test
    @WithMockUser(username = "academy@example.com")
    void createAcademyProfile_withBlankAcademyName_returns400() throws Exception {
        var request = new CreateAcademyProfileRequest("", null, null, null);

        mockMvc.perform(post("/api/v1/profiles/academy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.academyName").exists());
    }

    @Test
    @WithMockUser(username = "academy@example.com")
    void createAcademyProfile_whenNameAlreadyExists_returns409() throws Exception {
        when(profileService.createAcademyProfile(eq("academy@example.com"), any()))
                .thenThrow(new AcademyNameAlreadyExistsException(
                        "An academy with this name already exists: Academia Preuniversitaria Lima"));

        var request = new CreateAcademyProfileRequest("Academia Preuniversitaria Lima", null, null, null);

        mockMvc.perform(post("/api/v1/profiles/academy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("An academy with this name already exists: Academia Preuniversitaria Lima"));
    }

    @Test
    @WithMockUser(username = "student@example.com")
    void createAcademyProfile_whenWrongProfileType_returns409() throws Exception {
        when(profileService.createAcademyProfile(eq("student@example.com"), any()))
                .thenThrow(new WrongProfileTypeException(
                        "Account type is not ACADEMY. Current type: STUDENT"));

        var request = new CreateAcademyProfileRequest("Academia Lima", null, null, null);

        mockMvc.perform(post("/api/v1/profiles/academy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Account type is not ACADEMY. Current type: STUDENT"));
    }

    @Test
    @WithMockUser(username = "academy@example.com")
    void createAcademyProfile_whenAlreadyExists_returns409() throws Exception {
        when(profileService.createAcademyProfile(eq("academy@example.com"), any()))
                .thenThrow(new AcademyProfileAlreadyExistsException(
                        "Academy profile already exists for this account."));

        var request = new CreateAcademyProfileRequest("Academia Lima", null, null, null);

        mockMvc.perform(post("/api/v1/profiles/academy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Academy profile already exists for this account."));
    }

    @Test
    @WithMockUser(username = "nuevo@example.com")
    void createAcademyProfile_whenBaseProfileNotFound_returns404() throws Exception {
        when(profileService.createAcademyProfile(eq("nuevo@example.com"), any()))
                .thenThrow(new ProfileNotFoundException("Profile not found for user: nuevo@example.com"));

        var request = new CreateAcademyProfileRequest("Academia Lima", null, null, null);

        mockMvc.perform(post("/api/v1/profiles/academy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void createAcademyProfile_withoutAuth_returns401() throws Exception {
        var request = new CreateAcademyProfileRequest("Academia Lima", null, null, null);

        mockMvc.perform(post("/api/v1/profiles/academy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // GET /profiles/me
    // =========================================================================

    @Test
    @WithMockUser(username = "student@example.com")
    void getMyProfile_withCompleteStudentProfile_returns200AndIsComplete() throws Exception {
        when(profileService.getMyProfile(eq("student@example.com")))
                .thenReturn(buildProfileMeResponse(ProfileType.STUDENT, true));

        mockMvc.perform(get("/api/v1/profiles/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileType").value("STUDENT"))
                .andExpect(jsonPath("$.profileComplete").value(true))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.displayName").value("Test User"));
    }

    @Test
    @WithMockUser(username = "incomplete@example.com")
    void getMyProfile_withIncompleteProfile_returns200AndIsNotComplete() throws Exception {
        when(profileService.getMyProfile(eq("incomplete@example.com")))
                .thenReturn(buildProfileMeResponse(ProfileType.TEACHER, false));

        mockMvc.perform(get("/api/v1/profiles/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileType").value("TEACHER"))
                .andExpect(jsonPath("$.profileComplete").value(false));
    }

    @Test
    void getMyProfile_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/profiles/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "noprofile@example.com")
    void getMyProfile_whenProfileNotFound_returns404() throws Exception {
        when(profileService.getMyProfile(eq("noprofile@example.com")))
                .thenThrow(new ProfileNotFoundException("Profile not found for user: noprofile@example.com"));

        mockMvc.perform(get("/api/v1/profiles/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Profile not found for user: noprofile@example.com"));
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

    private StudentProfileResponse buildStudentProfileResponse(String gradeLevel) {
        StudentProfile sp = StudentProfile.builder()
                .profileId(UUID.randomUUID())
                .gradeLevel(gradeLevel)
                .build();
        return new StudentProfileResponse(sp);
    }

    private StudentProfileResponse buildStudentProfileResponseFull(
            String gradeLevel, String schoolName, String studyShift,
            UUID targetUniversityId, UUID targetAreaId, UUID targetCareerId) {
        StudentProfile sp = StudentProfile.builder()
                .profileId(UUID.randomUUID())
                .gradeLevel(gradeLevel)
                .schoolName(schoolName)
                .studyShift(studyShift)
                .targetUniversityId(targetUniversityId)
                .targetAreaId(targetAreaId)
                .targetCareerId(targetCareerId)
                .build();
        return new StudentProfileResponse(sp);
    }

    private TeacherProfileResponse buildTeacherProfileResponse(String bioProfessional) {
        TeacherProfile tp = TeacherProfile.builder()
                .profileId(UUID.randomUUID())
                .bioProfessional(bioProfessional)
                .build();
        return new TeacherProfileResponse(tp);
    }

    private AcademyProfileResponse buildAcademyProfileResponse(
            String academyName, String ruc, String website, String contactEmail) {
        AcademyProfile ap = AcademyProfile.builder()
                .profileId(UUID.randomUUID())
                .academyName(academyName)
                .ruc(ruc)
                .website(website)
                .contactEmail(contactEmail)
                .build();
        return AcademyProfileResponse.from(ap);
    }

    private ProfileMeResponse buildProfileMeResponse(ProfileType type, boolean isProfileComplete) {
        Profile profile = Profile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .displayName("Test User")
                .profileType(type.name())
                .createdAt(LocalDateTime.now())
                .build();
        return new ProfileMeResponse(profile, isProfileComplete);
    }
}
