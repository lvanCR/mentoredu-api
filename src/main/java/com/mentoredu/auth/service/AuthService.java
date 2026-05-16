package com.mentoredu.auth.service;

import com.mentoredu.auth.dto.RegisterRequest;
import com.mentoredu.auth.dto.RegisterResponse;
import com.mentoredu.auth.entity.AuthProvider;
import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.entity.UserStatus;
import com.mentoredu.auth.exception.EmailAlreadyExistsException;
import com.mentoredu.auth.repository.RoleRepository;
import com.mentoredu.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Email already registered: " + normalizedEmail);
        }

        var role = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new IllegalStateException("Default role STUDENT not found. Run DB migrations."));

        var user = User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.EMAIL)
                .status(UserStatus.ACTIVE)
                .role(role)
                .build();

        var saved = userRepository.save(user);

        return RegisterResponse.builder()
                .id(saved.getId())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .email(saved.getEmail())
                .role(saved.getRole().getName())
                .status(saved.getStatus().name())
                .build();
    }
}
