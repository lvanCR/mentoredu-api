package com.mentoredu.billing.dto;

import com.mentoredu.billing.model.CoinPackage;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class CoinPackageResponse {

    private final UUID id;
    private final String name;
    private final Integer coins;
    private final BigDecimal price;
    private final Boolean active;

    public CoinPackageResponse(CoinPackage coinPackage) {
        this.id = coinPackage.getId();
        this.name = coinPackage.getName();
        this.coins = coinPackage.getCoins();
        this.price = coinPackage.getPrice();
        this.active = coinPackage.getActive();
    }
}
