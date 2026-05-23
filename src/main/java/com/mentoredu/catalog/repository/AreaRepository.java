package com.mentoredu.catalog.repository;

import com.mentoredu.catalog.model.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AreaRepository extends JpaRepository<Area, UUID> {
    List<Area> findByUniversityId(UUID universityId);
    boolean existsByUniversityIdAndName(UUID universityId, String name);

    /** Valida que un área pertenece a una universidad (RN-12, RN-23). */
    @Query("SELECT COUNT(a) > 0 FROM Area a WHERE a.id = :areaId AND a.university.id = :universityId")
    boolean existsByIdAndUniversityId(@Param("areaId") UUID areaId, @Param("universityId") UUID universityId);
}
