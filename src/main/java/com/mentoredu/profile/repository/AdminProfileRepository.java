package com.mentoredu.profile.repository;

import com.mentoredu.profile.model.AdminProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminProfileRepository extends JpaRepository<AdminProfile, UUID> {
}
