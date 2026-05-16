package com.mentoredu.academy.repository;

import com.mentoredu.academy.model.Cycle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CycleRepository extends JpaRepository<Cycle, UUID> {
    List<Cycle> findByAcademyId(UUID academyId);
}
