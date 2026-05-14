package com.mentoredu.gamification.controller;

import com.mentoredu.gamification.dto.CoinsRequest;
import com.mentoredu.gamification.dto.CoinsResponse;
import com.mentoredu.gamification.dto.PointsRequest;
import com.mentoredu.gamification.dto.PointsResponse;
import com.mentoredu.gamification.service.IGamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gamification/users/{userId}")
@RequiredArgsConstructor
public class GamificationController {

    private final IGamificationService gamificationService;

    @GetMapping("/points")
    public ResponseEntity<PointsResponse> getPoints(@PathVariable UUID userId) {
        return ResponseEntity.ok(gamificationService.getPoints(userId));
    }

    @PostMapping("/points/add")
    public ResponseEntity<PointsResponse> addPoints(@PathVariable UUID userId,
                                                     @RequestBody PointsRequest request) {
        return ResponseEntity.ok(gamificationService.addPoints(userId, request));
    }

    @GetMapping("/coins")
    public ResponseEntity<CoinsResponse> getCoins(@PathVariable UUID userId) {
        return ResponseEntity.ok(gamificationService.getCoins(userId));
    }

    @PostMapping("/coins/add")
    public ResponseEntity<CoinsResponse> addCoins(@PathVariable UUID userId,
                                                   @RequestBody CoinsRequest request) {
        return ResponseEntity.ok(gamificationService.addCoins(userId, request));
    }

    @PostMapping("/coins/redeem")
    public ResponseEntity<CoinsResponse> redeemCoins(@PathVariable UUID userId,
                                                      @RequestBody CoinsRequest request) {
        return ResponseEntity.ok(gamificationService.redeemCoins(userId, request));
    }
}
