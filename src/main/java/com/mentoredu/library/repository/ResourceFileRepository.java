package com.mentoredu.library.repository;

import com.mentoredu.library.model.ResourceFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResourceFileRepository extends JpaRepository<ResourceFile, UUID> {
}
