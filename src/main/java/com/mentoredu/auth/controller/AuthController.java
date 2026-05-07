package com.mentoredu.auth.controller;

import com.mentoredu.auth.model.User;
import com.mentoredu.auth.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User created = userService.register(user);
        // Ocultamos la contraseña antes de devolver (aunque más adelante usaremos DTOs)
        created.setPassword(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        User loggedIn = userService.login(email, password);
        loggedIn.setPassword(null);
        return ResponseEntity.ok(loggedIn);
    }
}