package com.mentoredu.content.repository;

import com.mentoredu.content.model.ResourceFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResourceFileRepository extends JpaRepository<ResourceFile, UUID> {
}
