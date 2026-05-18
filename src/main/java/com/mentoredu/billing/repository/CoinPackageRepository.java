package com.mentoredu.billing.repository;

import com.mentoredu.billing.model.CoinPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CoinPackageRepository extends JpaRepository<CoinPackage, UUID> {
    List<CoinPackage> findByActiveTrue();
}
