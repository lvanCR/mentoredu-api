package com.mentoredu.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.dto.RegisterRequest;
import com.mentoredu.auth.model.Role;
import com.mentoredu.auth.model.User;
import com.mentoredu.auth.service.IUserService;
import com.mentoredu.auth.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = AuthController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IUserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private User buildUser(String email) {
        Role role = Role.builder().id(UUID.randomUUID()).name("STUDENT").build();
        return User.builder()
                .id(UUID.randomUUID())
                .firstName("Ana")
                .lastName("García")
                .email(email)
                .passwordHash("$2a$hashed")
                .role(role)
                .build();
    }

    @Test
    void register_withValidData_returns201WithToken() throws Exception {
        String email = "ana@test.com";
        User user = buildUser(email);

        when(userService.register(any(RegisterRequest.class))).thenReturn(user);
        when(jwtUtil.generateToken(email)).thenReturn("mocked-jwt-token");

        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Ana");
        request.setLastName("García");
        request.setEmail(email);
        request.setPassword("Secret123!");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void register_withMissingFields_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("noemail");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withValidCredentials_returns200WithToken() throws Exception {
        String email = "ana@test.com";
        String password = "Secret123!";
        User user = buildUser(email);

        when(userService.login(eq(email), eq(password))).thenReturn(user);
        when(jwtUtil.generateToken(email)).thenReturn("mocked-jwt-token");

        Map<String, String> credentials = Map.of("email", email, "password", password);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.firstName").value("Ana"));
    }
}
