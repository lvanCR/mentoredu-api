package com.mentoredu.gamification.service;

import com.mentoredu.gamification.dto.CoinsRequest;
import com.mentoredu.gamification.dto.CoinsResponse;
import com.mentoredu.gamification.dto.PointsRequest;
import com.mentoredu.gamification.dto.PointsResponse;
import com.mentoredu.gamification.model.CoinWallet;
import com.mentoredu.gamification.model.PointTransaction;
import com.mentoredu.gamification.repository.CoinWalletRepository;
import com.mentoredu.gamification.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GamificationService implements IGamificationService {

    private final CoinWalletRepository coinWalletRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Override
    public PointsResponse getPoints(UUID userId) {
        Integer total = pointTransactionRepository.sumPointsByUserId(userId);
        return new PointsResponse(userId, total == null ? 0 : total);
    }

    @Override
    @Transactional
    public PointsResponse addPoints(UUID userId, PointsRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
        pointTransactionRepository.save(PointTransaction.builder()
                .userId(userId)
                .pointsDelta(request.getAmount())
                .reason("MANUAL_ADD")
                .build());
        return getPoints(userId);
    }

    @Override
    public CoinsResponse getCoins(UUID userId) {
        CoinWallet wallet = getWalletOrThrow(userId);
        return new CoinsResponse(userId, wallet.getBalance());
    }

    @Override
    @Transactional
    public CoinsResponse addCoins(UUID userId, CoinsRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
        CoinWallet wallet = getWalletOrThrow(userId);
        wallet.setBalance(wallet.getBalance() + request.getAmount());
        coinWalletRepository.save(wallet);
        return new CoinsResponse(userId, wallet.getBalance());
    }

    @Override
    @Transactional
    public CoinsResponse redeemCoins(UUID userId, CoinsRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
        CoinWallet wallet = getWalletOrThrow(userId);
        if (wallet.getBalance() < request.getAmount()) {
            throw new IllegalArgumentException("Saldo insuficiente de monedas");
        }
        wallet.setBalance(wallet.getBalance() - request.getAmount());
        coinWalletRepository.save(wallet);
        return new CoinsResponse(userId, wallet.getBalance());
    }

    private CoinWallet getWalletOrThrow(UUID userId) {
        return coinWalletRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet no encontrada para el usuario"));
    }
}
