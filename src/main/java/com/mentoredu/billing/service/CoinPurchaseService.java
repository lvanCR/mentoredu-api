package com.mentoredu.billing.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.billing.dto.BuyCoinPackageRequest;
import com.mentoredu.billing.dto.CoinPackageResponse;
import com.mentoredu.billing.dto.CoinPurchaseResponse;
import com.mentoredu.billing.exception.CoinPackageNotFoundException;
import com.mentoredu.billing.exception.PlanNotFoundException;
import com.mentoredu.billing.model.CoinPackage;
import com.mentoredu.billing.model.CoinPurchase;
import com.mentoredu.billing.model.Payment;
import com.mentoredu.billing.model.enums.CoinPurchaseStatus;
import com.mentoredu.billing.model.enums.PaymentStatus;
import com.mentoredu.billing.repository.CoinPackageRepository;
import com.mentoredu.billing.repository.CoinPurchaseRepository;
import com.mentoredu.billing.repository.PaymentRepository;
import com.mentoredu.gamification.model.CoinWallet;
import com.mentoredu.gamification.repository.CoinWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CoinPurchaseService implements ICoinPurchaseService {

    private final CoinPurchaseRepository coinPurchaseRepository;
    private final CoinPackageRepository coinPackageRepository;
    private final PaymentRepository paymentRepository;
    private final CoinWalletRepository coinWalletRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CoinPurchaseResponse buy(String userEmail, BuyCoinPackageRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new PlanNotFoundException("Usuario no encontrado: " + userEmail));

        CoinPackage coinPackage = coinPackageRepository.findById(request.getCoinPackageId())
                .filter(CoinPackage::getActive)
                .orElseThrow(() -> new CoinPackageNotFoundException(
                        "Paquete de monedas no encontrado o inactivo: " + request.getCoinPackageId()));

        int totalCoins = coinPackage.getCoins() * request.getQuantity();
        BigDecimal totalAmount = coinPackage.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        CoinPurchase purchase = coinPurchaseRepository.save(
                CoinPurchase.builder()
                        .user(user)
                        .coinPackage(coinPackage)
                        .quantity(request.getQuantity())
                        .totalAmount(totalAmount)
                        .status(CoinPurchaseStatus.PENDING)
                        .build());

        // Pago simulado como aprobado (RN-28 — compras fallidas no acreditan saldo)
        paymentRepository.save(
                Payment.builder()
                        .user(user)
                        .coinPurchaseId(purchase.getId())
                        .amount(totalAmount)
                        .currency("PEN")
                        .paymentMethod(request.getPaymentMethod())
                        .status(PaymentStatus.APPROVED)
                        .build());

        // Acreditar monedas en el wallet, creándolo si aún no existe
        CoinWallet wallet = coinWalletRepository.findById(user.getId())
                .orElseGet(() -> CoinWallet.builder().userId(user.getId()).balance(0).build());
        wallet.setBalance(wallet.getBalance() + totalCoins);
        coinWalletRepository.save(wallet);

        purchase.setStatus(CoinPurchaseStatus.COMPLETED);
        coinPurchaseRepository.save(purchase);

        return new CoinPurchaseResponse(purchase, wallet.getBalance());
    }

    @Override
    public List<CoinPackageResponse> listActivePackages() {
        return coinPackageRepository.findByActiveTrue().stream()
                .map(CoinPackageResponse::new)
                .toList();
    }
}
