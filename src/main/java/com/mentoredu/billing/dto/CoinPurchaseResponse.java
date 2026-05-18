package com.mentoredu.billing.dto;

import com.mentoredu.billing.model.CoinPurchase;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class CoinPurchaseResponse {

    private final UUID id;
    private final UUID userId;
    private final UUID coinPackageId;
    private final String coinPackageName;
    private final Integer quantity;
    private final Integer totalCoins;
    private final BigDecimal totalAmount;
    private final String status;
    private final Integer newWalletBalance;
    private final LocalDateTime createdAt;

    public CoinPurchaseResponse(CoinPurchase purchase, int newWalletBalance) {
        this.id = purchase.getId();
        this.userId = purchase.getUser().getId();
        this.coinPackageId = purchase.getCoinPackage().getId();
        this.coinPackageName = purchase.getCoinPackage().getName();
        this.quantity = purchase.getQuantity();
        this.totalCoins = purchase.getCoinPackage().getCoins() * purchase.getQuantity();
        this.totalAmount = purchase.getTotalAmount();
        this.status = purchase.getStatus().name();
        this.newWalletBalance = newWalletBalance;
        this.createdAt = purchase.getCreatedAt();
    }
}
