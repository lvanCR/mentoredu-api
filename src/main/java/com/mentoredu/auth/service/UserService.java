package com.mentoredu.auth.service;

import com.mentoredu.auth.model.Role;
import com.mentoredu.auth.model.User;
import com.mentoredu.auth.repository.RoleRepository;
import com.mentoredu.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Role defaultRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RuntimeException("Rol STUDENT no encontrado"));

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(defaultRole);
        // Si el frontend envió algún rol, se ignora y se fuerza STUDENT (por ahora)
        return userRepository.save(user);
    }

    @Override
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }
        // No devolver la contraseña en la respuesta (ya la anotaremos con @JsonIgnore o usaremos un DTO más adelante)
        return user;
    }
}