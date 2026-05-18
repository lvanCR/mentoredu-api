package com.mentoredu.billing.service;

import com.mentoredu.billing.dto.BuyCoinPackageRequest;
import com.mentoredu.billing.dto.CoinPackageResponse;
import com.mentoredu.billing.dto.CoinPurchaseResponse;

import java.util.List;

public interface ICoinPurchaseService {
    CoinPurchaseResponse buy(String userEmail, BuyCoinPackageRequest request);
    List<CoinPackageResponse> listActivePackages();
}
