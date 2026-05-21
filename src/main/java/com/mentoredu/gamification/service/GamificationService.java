package com.mentoredu.gamification.service;

import com.mentoredu.gamification.dto.BadgeResponse;
import com.mentoredu.gamification.dto.CoinsRequest;
import com.mentoredu.gamification.dto.CoinsResponse;
import com.mentoredu.gamification.dto.LevelProgressResponse;
import com.mentoredu.gamification.dto.PointsResponse;
import com.mentoredu.gamification.model.Badge;
import com.mentoredu.gamification.model.CoinWallet;
import com.mentoredu.gamification.model.LevelProgress;
import com.mentoredu.gamification.model.PointTransaction;
import com.mentoredu.gamification.model.UserBadge;
import com.mentoredu.gamification.model.enums.PointSourceType;
import com.mentoredu.gamification.repository.BadgeRepository;
import com.mentoredu.gamification.repository.CoinWalletRepository;
import com.mentoredu.gamification.repository.LevelProgressRepository;
import com.mentoredu.gamification.repository.PointTransactionRepository;
import com.mentoredu.gamification.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GamificationService implements IGamificationService {

    // 1 level per 100 XP, max level 50
    private static final int XP_PER_LEVEL = 100;
    private static final int MAX_LEVEL = 50;

    private final CoinWalletRepository coinWalletRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final LevelProgressRepository levelProgressRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

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
        // RN-31: check badge milestones after each XP event (US32)
        checkAndAwardBadges(userId, sourceType);
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PointsResponse getPoints(UUID userId) {
        Integer total = pointTransactionRepository.sumPointsByUserId(userId);
        return new PointsResponse(userId, total == null ? 0 : total);
    }

    @Override
    @Transactional(readOnly = true)
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
    // US31 — View personal level and progress
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public LevelProgressResponse getLevelProgress(UUID userId) {
        LevelProgress progress = levelProgressRepository.findById(userId)
                .orElseGet(() -> {
                    // Gherkin alternativo error: registro no existe → inicializar y devolver estado base
                    LevelProgress base = LevelProgress.builder()
                            .userId(userId)
                            .currentLevel(1)
                            .experience(0)
                            .progressPercentage(BigDecimal.ZERO)
                            .build();
                    return levelProgressRepository.save(base);
                });
        return new LevelProgressResponse(
                progress.getUserId(),
                progress.getCurrentLevel(),
                progress.getExperience(),
                progress.getProgressPercentage(),
                progress.getUpdatedAt()
        );
    }

    // -------------------------------------------------------------------------
    // US32 — Earn and view badges
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<BadgeResponse> getBadges(UUID userId) {
        List<UserBadge> userBadges = userBadgeRepository.findByUserId(userId);
        Set<UUID> badgeIds = userBadges.stream().map(UserBadge::getBadgeId).collect(Collectors.toSet());
        Map<UUID, Badge> badgeMap = badgeRepository.findAllById(badgeIds).stream()
                .collect(Collectors.toMap(Badge::getId, Function.identity()));
        return userBadges.stream()
                .map(ub -> {
                    Badge badge = badgeMap.get(ub.getBadgeId());
                    if (badge == null) throw new IllegalStateException("Badge not found: " + ub.getBadgeId());
                    return new BadgeResponse(badge.getId(), badge.getCode(), badge.getName(),
                            badge.getDescription(), ub.getEarnedAt());
                })
                .toList();
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

    // RN-31: badge milestones evaluated internally after each XP event.
    // RN-33: each badge is awarded at most once per user (duplicate silently discarded).
    private void checkAndAwardBadges(UUID userId, PointSourceType sourceType) {
        long answerCount = pointTransactionRepository
                .countByUserIdAndSourceType(userId, PointSourceType.ANSWER_GIVEN.name());
        long resourceCount = pointTransactionRepository
                .countByUserIdAndSourceType(userId, PointSourceType.RESOURCE_PUBLISHED.name());
        long solutionCount = pointTransactionRepository
                .countByUserIdAndSourceType(userId, PointSourceType.SOLUTION_SUBMITTED.name());
        int totalXP = pointTransactionRepository.sumPointsByUserId(userId);
        int currentLevel = Math.min(totalXP / XP_PER_LEVEL + 1, MAX_LEVEL);

        if (answerCount >= 1) tryAwardBadge(userId, "FIRST_ANSWER");
        if (resourceCount >= 1) tryAwardBadge(userId, "FIRST_RESOURCE");
        if (solutionCount >= 1) tryAwardBadge(userId, "FIRST_SOLUTION");
        if (currentLevel >= 5) tryAwardBadge(userId, "LEVEL_5");
        if (currentLevel >= 10) tryAwardBadge(userId, "LEVEL_10");
    }

    @Override
    @Transactional
    public void awardBadge(UUID userId, String badgeCode) {
        tryAwardBadge(userId, badgeCode);
    }

    private void tryAwardBadge(UUID userId, String badgeCode) {
        badgeRepository.findByCode(badgeCode).ifPresent(badge -> {
            if (!userBadgeRepository.existsByUserIdAndBadgeId(userId, badge.getId())) {
                userBadgeRepository.save(UserBadge.builder()
                        .userId(userId)
                        .badgeId(badge.getId())
                        .build());
            }
        });
    }
}
