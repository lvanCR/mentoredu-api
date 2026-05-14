package com.mentoredu.auth.service;

import com.mentoredu.auth.dto.RegisterRequest;
import com.mentoredu.auth.model.Role;
import com.mentoredu.auth.model.User;
import com.mentoredu.auth.repository.RoleRepository;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.gamification.model.CoinWallet;
import com.mentoredu.gamification.model.LevelProgress;
import com.mentoredu.gamification.repository.CoinWalletRepository;
import com.mentoredu.gamification.repository.LevelProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CoinWalletRepository coinWalletRepository;
    private final LevelProgressRepository levelProgressRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().getName())
                .build();
    }

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Role defaultRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RuntimeException("Rol STUDENT no encontrado"));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(defaultRole)
                .build();

        User saved = userRepository.save(user);

        coinWalletRepository.save(CoinWallet.builder()
                .userId(saved.getId())
                .balance(0)
                .build());

        levelProgressRepository.save(LevelProgress.builder()
                .userId(saved.getId())
                .currentLevel(1)
                .experience(0)
                .progressPercentage(java.math.BigDecimal.ZERO)
                .build());

        return saved;
    }

    @Override
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }
        return user;
    }
}
