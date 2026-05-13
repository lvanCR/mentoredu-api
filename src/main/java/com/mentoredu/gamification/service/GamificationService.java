package com.mentoredu.gamification.service;

import com.mentoredu.auth.model.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.gamification.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class GamificationService {
    @Autowired
    private UserRepository userRepository;

    public PointsResponse getPoints(java.util.UUID userId) {
        User user = getUserOrThrow(userId);
        return new PointsResponse(user.getId(), user.getPoints());
    }

    @Transactional
    public PointsResponse addPoints(java.util.UUID userId, PointsRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
        User user = getUserOrThrow(userId);
        user.setPoints(user.getPoints() + request.getAmount());
        userRepository.save(user);
        return new PointsResponse(user.getId(), user.getPoints());
    }

    public CoinsResponse getCoins(java.util.UUID userId) {
        User user = getUserOrThrow(userId);
        return new CoinsResponse(user.getId(), user.getCoins());
    }

    @Transactional
    public CoinsResponse addCoins(java.util.UUID userId, CoinsRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
        User user = getUserOrThrow(userId);
        user.setCoins(user.getCoins() + request.getAmount());
        userRepository.save(user);
        return new CoinsResponse(user.getId(), user.getCoins());
    }

    @Transactional
    public CoinsResponse redeemCoins(java.util.UUID userId, CoinsRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
        User user = getUserOrThrow(userId);
        if (user.getCoins() < request.getAmount()) {
            throw new IllegalArgumentException("Saldo insuficiente de monedas");
        }
        user.setCoins(user.getCoins() - request.getAmount());
        userRepository.save(user);
        return new CoinsResponse(user.getId(), user.getCoins());
    }

    private User getUserOrThrow(java.util.UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario inexistente"));
    }
}
