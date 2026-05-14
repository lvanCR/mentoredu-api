package com.mentoredu.auth.controller;

import com.mentoredu.auth.dto.LoginResponse;
import com.mentoredu.auth.dto.RegisterRequest;
import com.mentoredu.auth.model.User;
import com.mentoredu.auth.service.IUserService;
import com.mentoredu.auth.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        User created = userService.register(request);
        String token = jwtUtil.generateToken(created.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponse(
                token,
                created.getId(),
                created.getFirstName(),
                created.getLastName(),
                created.getEmail(),
                created.getRole().getName()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        User loggedIn = userService.login(email, password);
        String token = jwtUtil.generateToken(loggedIn.getEmail());
        return ResponseEntity.ok(new LoginResponse(
                token,
                loggedIn.getId(),
                loggedIn.getFirstName(),
                loggedIn.getLastName(),
                loggedIn.getEmail(),
                loggedIn.getRole().getName()
        ));
    }
}
