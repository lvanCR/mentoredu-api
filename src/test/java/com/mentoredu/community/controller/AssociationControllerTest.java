package com.mentoredu.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.community.dto.AssociationResponse;
import com.mentoredu.community.dto.CreateAssociationRequest;
import com.mentoredu.community.exception.AssociationNotFoundException;
import com.mentoredu.community.exception.DuplicateAssociationException;
import com.mentoredu.community.model.TeacherAcademyLink;
import com.mentoredu.community.repository.TeacherAcademyLinkRepository;
import com.mentoredu.profile.model.AcademyProfile;
import com.mentoredu.profile.model.Profile;
import com.mentoredu.profile.repository.AcademyProfileRepository;
import com.mentoredu.profile.repository.ProfileRepository;
import com.mentoredu.profile.repository.TeacherProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AssociationController.class)
class AssociationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private TeacherAcademyLinkRepository linkRepository;
    @MockitoBean private ProfileRepository            profileRepository;
    @MockitoBean private TeacherProfileRepository     teacherProfileRepository;
    @MockitoBean private AcademyProfileRepository     academyProfileRepository;
    @MockitoBean private UserRepository               userRepository;
    @MockitoBean private JwtUtil                      jwtUtil;
    @MockitoBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // =========================================================================
    // US24 — Docente solicita asociarse
    // =========================================================================

    @Test
    @WithMockUser(username = "teacher@example.com")
    void requestAssociation_asTeacher_returns201() throws Exception {
        UUID academyProfileId = UUID.randomUUID();
        User user = buildUser("teacher@example.com");
        Profile profile = buildProfile(user.getId(), "TEACHER");
        AcademyProfile academy = buildAcademyProfile(academyProfileId);
        TeacherAcademyLink link = buildLink(profile.getId(), academyProfileId);

        when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));
        when(teacherProfileRepository.existsById(profile.getId())).thenReturn(true);
        when(academyProfileRepository.findById(academyProfileId)).thenReturn(Optional.of(academy));
        when(linkRepository.findByTeacherProfileIdAndAcademyProfileId(
                profile.getId(), academyProfileId)).thenReturn(Optional.empty());
        when(linkRepository.save(any())).thenReturn(link);

        var request = new CreateAssociationRequest();
        request.setAcademyProfileId(academyProfileId);

        mockMvc.perform(post("/api/v1/associations/teacher-academy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void requestAssociation_whenDuplicate_returns409() throws Exception {
        UUID academyProfileId = UUID.randomUUID();
        User user = buildUser("teacher@example.com");
        Profile profile = buildProfile(user.getId(), "TEACHER");
        AcademyProfile academy = buildAcademyProfile(academyProfileId);

        when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));
        when(teacherProfileRepository.existsById(profile.getId())).thenReturn(true);
        when(academyProfileRepository.findById(academyProfileId)).thenReturn(Optional.of(academy));
        when(linkRepository.findByTeacherProfileIdAndAcademyProfileId(
                profile.getId(), academyProfileId))
                .thenReturn(Optional.of(buildLink(profile.getId(), academyProfileId)));

        var request = new CreateAssociationRequest();
        request.setAcademyProfileId(academyProfileId);

        mockMvc.perform(post("/api/v1/associations/teacher-academy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @WithMockUser(username = "teacher@example.com")
    void requestAssociation_asNonTeacher_returns403() throws Exception {
        UUID academyProfileId = UUID.randomUUID();
        User user = buildUser("teacher@example.com");
        Profile profile = buildProfile(user.getId(), "STUDENT");

        when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));
        when(teacherProfileRepository.existsById(profile.getId())).thenReturn(false);

        var request = new CreateAssociationRequest();
        request.setAcademyProfileId(academyProfileId);

        mockMvc.perform(post("/api/v1/associations/teacher-academy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestAssociation_withoutAuth_returns401() throws Exception {
        var request = new CreateAssociationRequest();
        request.setAcademyProfileId(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/associations/teacher-academy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US24 — Academia acepta solicitud
    // =========================================================================

    @Test
    @WithMockUser(username = "academy@example.com")
    void acceptAssociation_returns200WithAccepted() throws Exception {
        User user = buildUser("academy@example.com");
        UUID academyProfileId = UUID.randomUUID();
        Profile profile = buildProfile(user.getId(), "ACADEMY");
        profile.setId(academyProfileId);
        TeacherAcademyLink link = buildLink(UUID.randomUUID(), academyProfileId);

        when(userRepository.findByEmail("academy@example.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));
        when(academyProfileRepository.existsById(academyProfileId)).thenReturn(true);
        when(linkRepository.findById(link.getId())).thenReturn(Optional.of(link));

        TeacherAcademyLink accepted = TeacherAcademyLink.builder()
                .id(link.getId())
                .teacherProfileId(link.getTeacherProfileId())
                .academyProfileId(academyProfileId)
                .status("ACCEPTED")
                .requestedAt(link.getRequestedAt())
                .resolvedAt(LocalDateTime.now())
                .build();
        when(linkRepository.save(any())).thenReturn(accepted);

        mockMvc.perform(patch("/api/v1/associations/teacher-academy/{id}/accept", link.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @WithMockUser(username = "academy@example.com")
    void acceptAssociation_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        User user = buildUser("academy@example.com");
        UUID academyProfileId = UUID.randomUUID();
        Profile profile = buildProfile(user.getId(), "ACADEMY");
        profile.setId(academyProfileId);

        when(userRepository.findByEmail("academy@example.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));
        when(academyProfileRepository.existsById(academyProfileId)).thenReturn(true);
        when(linkRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/v1/associations/teacher-academy/{id}/accept", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void acceptAssociation_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/associations/teacher-academy/{id}/accept", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private User buildUser(String email) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .firstName("Test")
                .lastName("User")
                .build();
    }

    private Profile buildProfile(UUID userId, String profileType) {
        return Profile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .displayName("Test User")
                .profileType(profileType)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private AcademyProfile buildAcademyProfile(UUID profileId) {
        return AcademyProfile.builder()
                .profileId(profileId)
                .academyName("Academia Test")
                .build();
    }

    private TeacherAcademyLink buildLink(UUID teacherProfileId, UUID academyProfileId) {
        return TeacherAcademyLink.builder()
                .id(UUID.randomUUID())
                .teacherProfileId(teacherProfileId)
                .academyProfileId(academyProfileId)
                .status("PENDING")
                .requestedAt(LocalDateTime.now())
                .build();
    }
}
