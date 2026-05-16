package com.mentoredu.gamification.repository;

import com.mentoredu.gamification.model.CoinWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CoinWalletRepository extends JpaRepository<CoinWallet, UUID> {
}
