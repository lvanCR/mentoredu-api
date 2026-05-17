package com.mentoredu.academy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.academy.dto.AcademyResponse;
import com.mentoredu.academy.dto.CreateAcademyRequest;
import com.mentoredu.academy.dto.CreateCycleRequest;
import com.mentoredu.academy.dto.CreateProgramRequest;
import com.mentoredu.academy.dto.CycleResponse;
import com.mentoredu.academy.dto.ProgramResponse;
import com.mentoredu.academy.exception.AcademyAlreadyExistsException;
import com.mentoredu.academy.exception.AcademyNotFoundException;
import com.mentoredu.academy.exception.CycleAlreadyExistsException;
import com.mentoredu.academy.exception.ProgramAlreadyExistsException;
import com.mentoredu.academy.model.Academy;
import com.mentoredu.academy.model.Cycle;
import com.mentoredu.academy.model.Program;
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

import java.time.LocalDate;
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

    @Test
    void createAcademy_withoutAuth_returns401() throws Exception {
        var request = academyRequest("Academia Norte", null, null, null);

        mockMvc.perform(post("/api/v1/academies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US11 — Register academic offering: program
    // =========================================================================

    // -------------------------------------------------------------------------
    // Escenario exitoso: programa con todos los campos → 201
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "org@example.com")
    void createProgram_withValidRequest_returns201() throws Exception {
        UUID academyId = UUID.randomUUID();
        var request  = programRequest("Preparatorio UNI", "PRESENCIAL", "INTENSIVO", "UNI");
        var response = buildProgramResponse(academyId, "Preparatorio UNI", "PRESENCIAL", "INTENSIVO", "UNI");
        when(academyService.createProgram(eq("org@example.com"), eq(academyId), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/academies/{academyId}/programs", academyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.academyId").value(academyId.toString()))
                .andExpect(jsonPath("$.name").value("Preparatorio UNI"))
                .andExpect(jsonPath("$.modality").value("PRESENCIAL"))
                .andExpect(jsonPath("$.intensity").value("INTENSIVO"))
                .andExpect(jsonPath("$.targetUniversity").value("UNI"));
    }

    // -------------------------------------------------------------------------
    // Escenario error: campo obligatorio faltante → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "org@example.com")
    void createProgram_withBlankName_returns400() throws Exception {
        UUID academyId = UUID.randomUUID();
        var request = programRequest("", "PRESENCIAL", "INTENSIVO", "UNI");

        mockMvc.perform(post("/api/v1/academies/{academyId}/programs", academyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.name").exists());
    }

    @Test
    @WithMockUser(username = "org@example.com")
    void createProgram_withMissingModality_returns400() throws Exception {
        UUID academyId = UUID.randomUUID();
        String body = "{\"name\":\"Preparatorio\",\"intensity\":\"NORMAL\",\"targetUniversity\":\"PUCP\"}";

        mockMvc.perform(post("/api/v1/academies/{academyId}/programs", academyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.modality").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario alternativo error: programa duplicado → 409
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "org@example.com")
    void createProgram_whenDuplicate_returns409() throws Exception {
        UUID academyId = UUID.randomUUID();
        when(academyService.createProgram(eq("org@example.com"), eq(academyId), any()))
                .thenThrow(new ProgramAlreadyExistsException(
                        "A program with this name already exists in the academy: Preparatorio UNI"));

        var request = programRequest("Preparatorio UNI", "PRESENCIAL", "INTENSIVO", "UNI");

        mockMvc.perform(post("/api/v1/academies/{academyId}/programs", academyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "A program with this name already exists in the academy: Preparatorio UNI"));
    }

    // -------------------------------------------------------------------------
    // Escenario error: academia no encontrada → 404
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "org@example.com")
    void createProgram_whenAcademyNotFound_returns404() throws Exception {
        UUID academyId = UUID.randomUUID();
        when(academyService.createProgram(eq("org@example.com"), eq(academyId), any()))
                .thenThrow(new AcademyNotFoundException(
                        "Academy not found: " + academyId));

        var request = programRequest("Preparatorio UNI", "PRESENCIAL", "INTENSIVO", "UNI");

        mockMvc.perform(post("/api/v1/academies/{academyId}/programs", academyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // -------------------------------------------------------------------------
    // Sin autenticación → 401
    // -------------------------------------------------------------------------

    @Test
    void createProgram_withoutAuth_returns401() throws Exception {
        UUID academyId = UUID.randomUUID();
        var request = programRequest("Preparatorio UNI", "PRESENCIAL", "INTENSIVO", "UNI");

        mockMvc.perform(post("/api/v1/academies/{academyId}/programs", academyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US11 — Register academic offering: cycle
    // =========================================================================

    // -------------------------------------------------------------------------
    // Escenario exitoso: ciclo con todos los campos → 201
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "org@example.com")
    void createCycle_withValidRequest_returns201() throws Exception {
        UUID academyId = UUID.randomUUID();
        var request  = cycleRequest("Ciclo Intensivo Verano 2026", "INTENSIVO", "2026-01-06", "2026-03-31");
        var response = buildCycleResponse(academyId,
                "Ciclo Intensivo Verano 2026", "INTENSIVO", "2026-01-06", "2026-03-31");
        when(academyService.createCycle(eq("org@example.com"), eq(academyId), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/academies/{academyId}/cycles", academyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.academyId").value(academyId.toString()))
                .andExpect(jsonPath("$.name").value("Ciclo Intensivo Verano 2026"))
                .andExpect(jsonPath("$.cycleType").value("INTENSIVO"));
    }

    // -------------------------------------------------------------------------
    // Escenario error: formato de fecha inválido → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "org@example.com")
    void createCycle_withInvalidDateFormat_returns400() throws Exception {
        UUID academyId = UUID.randomUUID();
        var request = cycleRequest("Ciclo Verano", "REGULAR", "06-01-2026", "31-03-2026");

        mockMvc.perform(post("/api/v1/academies/{academyId}/cycles", academyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario error: campos obligatorios faltantes → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "org@example.com")
    void createCycle_withBlankName_returns400() throws Exception {
        UUID academyId = UUID.randomUUID();
        var request = cycleRequest("", "REGULAR", "2026-01-06", "2026-03-31");

        mockMvc.perform(post("/api/v1/academies/{academyId}/cycles", academyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.name").exists());
    }

    // -------------------------------------------------------------------------
    // Escenario alternativo error: ciclo duplicado → 409
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "org@example.com")
    void createCycle_whenDuplicate_returns409() throws Exception {
        UUID academyId = UUID.randomUUID();
        when(academyService.createCycle(eq("org@example.com"), eq(academyId), any()))
                .thenThrow(new CycleAlreadyExistsException(
                        "A cycle with this name already exists in the academy: Ciclo Intensivo Verano 2026"));

        var request = cycleRequest("Ciclo Intensivo Verano 2026", "INTENSIVO", "2026-01-06", "2026-03-31");

        mockMvc.perform(post("/api/v1/academies/{academyId}/cycles", academyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "A cycle with this name already exists in the academy: Ciclo Intensivo Verano 2026"));
    }

    // -------------------------------------------------------------------------
    // Escenario error: academia no encontrada → 404
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "org@example.com")
    void createCycle_whenAcademyNotFound_returns404() throws Exception {
        UUID academyId = UUID.randomUUID();
        when(academyService.createCycle(eq("org@example.com"), eq(academyId), any()))
                .thenThrow(new AcademyNotFoundException(
                        "Academy not found: " + academyId));

        var request = cycleRequest("Ciclo Intensivo Verano 2026", "INTENSIVO", "2026-01-06", "2026-03-31");

        mockMvc.perform(post("/api/v1/academies/{academyId}/cycles", academyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // -------------------------------------------------------------------------
    // Sin autenticación → 401
    // -------------------------------------------------------------------------

    @Test
    void createCycle_withoutAuth_returns401() throws Exception {
        UUID academyId = UUID.randomUUID();
        var request = cycleRequest("Ciclo Verano 2026", "REGULAR", "2026-01-06", "2026-03-31");

        mockMvc.perform(post("/api/v1/academies/{academyId}/cycles", academyId)
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

    private CreateProgramRequest programRequest(
            String name, String modality, String intensity, String targetUniversity) {
        var r = new CreateProgramRequest();
        r.setName(name);
        r.setModality(modality);
        r.setIntensity(intensity);
        r.setTargetUniversity(targetUniversity);
        return r;
    }

    private ProgramResponse buildProgramResponse(
            UUID academyId, String name, String modality, String intensity, String targetUniversity) {
        Program program = Program.builder()
                .id(UUID.randomUUID())
                .academyId(academyId)
                .name(name)
                .modality(modality)
                .intensity(intensity)
                .targetUniversity(targetUniversity)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return new ProgramResponse(program);
    }

    private CreateCycleRequest cycleRequest(
            String name, String cycleType, String startDate, String endDate) {
        var r = new CreateCycleRequest();
        r.setName(name);
        r.setCycleType(cycleType);
        r.setStartDate(startDate);
        r.setEndDate(endDate);
        return r;
    }

    private CycleResponse buildCycleResponse(
            UUID academyId, String name, String cycleType, String startDate, String endDate) {
        Cycle cycle = Cycle.builder()
                .id(UUID.randomUUID())
                .academyId(academyId)
                .name(name)
                .cycleType(cycleType)
                .startDate(LocalDate.parse(startDate))
                .endDate(LocalDate.parse(endDate))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return new CycleResponse(cycle);
    }
}
