package com.mentoredu.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.dto.RegisterRequest;
import com.mentoredu.auth.dto.RegisterResponse;
import com.mentoredu.auth.exception.EmailAlreadyExistsException;
import com.mentoredu.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @Test
    void register_withValidData_returns201AndUserInfo() throws Exception {
        var response = RegisterResponse.builder()
                .id(UUID.randomUUID())
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@example.com")
                .role("STUDENT")
                .status("ACTIVE")
                .build();

        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("juan@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void register_withDuplicateEmail_returns409() throws Exception {
        when(authService.register(any()))
                .thenThrow(new EmailAlreadyExistsException("Email already registered: juan@example.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void register_withBlankFirstName_returns400() throws Exception {
        var request = validRequest();
        request.setFirstName("");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.firstName").exists());
    }

    @Test
    void register_withInvalidEmail_returns400() throws Exception {
        var request = validRequest();
        request.setEmail("not-an-email");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.email").exists());
    }

    @Test
    void register_withWeakPassword_returns400() throws Exception {
        var request = validRequest();
        request.setPassword("weak");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.password").exists());
    }

    @Test
    void register_withPasswordMissingUppercase_returns400() throws Exception {
        var request = validRequest();
        request.setPassword("nouppercase1");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withPasswordMissingDigit_returns400() throws Exception {
        var request = validRequest();
        request.setPassword("NoDigitPassword");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private RegisterRequest validRequest() {
        var r = new RegisterRequest();
        r.setFirstName("Juan");
        r.setLastName("Pérez");
        r.setEmail("juan@example.com");
        r.setPassword("Password123");
        return r;
    }
}
