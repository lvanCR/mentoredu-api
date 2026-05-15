package com.mentoredu.profile.repository;

import com.mentoredu.profile.model.OrganizationProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrganizationProfileRepository extends JpaRepository<OrganizationProfile, UUID> {
}
