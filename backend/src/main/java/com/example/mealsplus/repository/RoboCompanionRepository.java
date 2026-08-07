package com.example.mealsplus.repository;

import com.example.mealsplus.domain.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface RoboCompanionRepository extends JpaRepository<RoboCompanion, Long> {
    Optional<RoboCompanion> findByAssetTagIgnoreCase(String assetTag);
    List<RoboCompanion> findByActiveTrueAndStatusOrderByName(RoboCompanionStatus status);
    long countByStatus(RoboCompanionStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from RoboCompanion r where r.id = :id")
    Optional<RoboCompanion> findByIdForUpdate(@Param("id") Long id);
}
