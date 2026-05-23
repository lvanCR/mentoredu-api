package com.mentoredu.catalog.repository;

import com.mentoredu.catalog.model.Career;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CareerRepository extends JpaRepository<Career, UUID> {
    List<Career> findByUniversityId(UUID universityId);
    List<Career> findByAreaId(UUID areaId);
    boolean existsByUniversityIdAndAreaIdAndName(UUID universityId, UUID areaId, String name);

    /** Valida que una carrera pertenece al área indicada (RN-23). */
    @Query("SELECT COUNT(c) > 0 FROM Career c WHERE c.id = :careerId AND c.area.id = :areaId")
    boolean existsByIdAndAreaId(@Param("careerId") UUID careerId, @Param("areaId") UUID areaId);

    /** Valida que una carrera pertenece a una universidad (RN-12). */
    @Query("SELECT COUNT(c) > 0 FROM Career c WHERE c.id = :careerId AND c.university.id = :universityId")
    boolean existsByIdAndUniversityId(@Param("careerId") UUID careerId, @Param("universityId") UUID universityId);
}
