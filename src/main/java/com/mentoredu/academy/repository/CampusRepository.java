package com.mentoredu.academy.repository;

import com.mentoredu.academy.model.Campus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CampusRepository extends JpaRepository<Campus, UUID> {
    List<Campus> findByAcademyId(UUID academyId);
    boolean existsByNameAndAcademyId(String name, UUID academyId);
}
