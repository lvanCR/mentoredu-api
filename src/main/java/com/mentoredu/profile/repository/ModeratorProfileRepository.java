package com.mentoredu.profile.repository;

import com.mentoredu.profile.model.ModeratorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ModeratorProfileRepository extends JpaRepository<ModeratorProfile, UUID> {
}
