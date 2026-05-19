package com.mentoredu.gamification.service;

import com.mentoredu.gamification.dto.CoinsRequest;
import com.mentoredu.gamification.dto.CoinsResponse;
import com.mentoredu.gamification.dto.PointsResponse;
import com.mentoredu.gamification.model.CoinWallet;
import com.mentoredu.gamification.model.LevelProgress;
import com.mentoredu.gamification.model.PointTransaction;
import com.mentoredu.gamification.model.enums.PointSourceType;
import com.mentoredu.gamification.repository.CoinWalletRepository;
import com.mentoredu.gamification.repository.LevelProgressRepository;
import com.mentoredu.gamification.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GamificationService implements IGamificationService {

    // 1 level per 100 XP, max level 50
    private static final int XP_PER_LEVEL = 100;
    private static final int MAX_LEVEL = 50;

    private final CoinWalletRepository coinWalletRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final LevelProgressRepository levelProgressRepository;

    // -------------------------------------------------------------------------
    // US30 — Earn experience points
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void awardPoints(UUID userId, PointSourceType sourceType, UUID sourceId, int pointsDelta) {
        // RN-34: immutable history — skip if this exact action already awarded points
        if (pointTransactionRepository.existsBySourceTypeAndSourceId(sourceType.name(), sourceId)) {
            return;
        }
        pointTransactionRepository.save(PointTransaction.builder()
                .userId(userId)
                .sourceType(sourceType.name())
                .sourceId(sourceId)
                .pointsDelta(pointsDelta)
                .reason(sourceType.name())
                .build());
        // RN-32: recalculate level automatically after new XP
        recalculateLevelProgress(userId);
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

    @Override
    public PointsResponse getPoints(UUID userId) {
        Integer total = pointTransactionRepository.sumPointsByUserId(userId);
        return new PointsResponse(userId, total == null ? 0 : total);
    }

    @Override
    public CoinsResponse getCoins(UUID userId) {
        return coinWalletRepository.findById(userId)
                .map(wallet -> new CoinsResponse(userId, wallet.getBalance()))
                .orElse(new CoinsResponse(userId, 0));
    }

    @Override
    @Transactional
    public CoinsResponse redeemCoins(UUID userId, CoinsRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
        CoinWallet wallet = coinWalletRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Saldo insuficiente de monedas"));
        if (wallet.getBalance() < request.getAmount()) {
            throw new IllegalArgumentException("Saldo insuficiente de monedas");
        }
        wallet.setBalance(wallet.getBalance() - request.getAmount());
        coinWalletRepository.save(wallet);
        return new CoinsResponse(userId, wallet.getBalance());
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void recalculateLevelProgress(UUID userId) {
        int totalXP = pointTransactionRepository.sumPointsByUserId(userId);
        int level = Math.min(totalXP / XP_PER_LEVEL + 1, MAX_LEVEL);
        int progressInLevel = totalXP % XP_PER_LEVEL;

        LevelProgress progress = levelProgressRepository.findById(userId)
                .orElse(LevelProgress.builder().userId(userId).build());
        progress.setCurrentLevel(level);
        progress.setExperience(totalXP);
        progress.setProgressPercentage(new BigDecimal(progressInLevel));
        levelProgressRepository.save(progress);
    }
}
