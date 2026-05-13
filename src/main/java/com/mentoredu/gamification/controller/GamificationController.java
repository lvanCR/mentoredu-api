package com.mentoredu.gamification.controller;

import com.mentoredu.gamification.dto.*;
import com.mentoredu.gamification.service.GamificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gamification/users/{userId}")
public class GamificationController {
    @Autowired
    private GamificationService gamificationService;

    @GetMapping("/points")
    public ResponseEntity<PointsResponse> getPoints(@PathVariable java.util.UUID userId) {
        return ResponseEntity.ok(gamificationService.getPoints(userId));
    }

    @PostMapping("/points/add")
    public ResponseEntity<PointsResponse> addPoints(@PathVariable java.util.UUID userId, @RequestBody PointsRequest request) {
        return ResponseEntity.ok(gamificationService.addPoints(userId, request));
    }

    @GetMapping("/coins")
    public ResponseEntity<CoinsResponse> getCoins(@PathVariable java.util.UUID userId) {
        return ResponseEntity.ok(gamificationService.getCoins(userId));
    }

    @PostMapping("/coins/add")
    public ResponseEntity<CoinsResponse> addCoins(@PathVariable java.util.UUID userId, @RequestBody CoinsRequest request) {
        return ResponseEntity.ok(gamificationService.addCoins(userId, request));
    }

    @PostMapping("/coins/redeem")
    public ResponseEntity<CoinsResponse> redeemCoins(@PathVariable java.util.UUID userId, @RequestBody CoinsRequest request) {
        return ResponseEntity.ok(gamificationService.redeemCoins(userId, request));
    }
}
