package com.example.mealsplus.repository;

import com.example.mealsplus.domain.*;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoboCompanionVisitRequestRepository extends JpaRepository<RoboCompanionVisitRequest, Long> {
    List<RoboCompanionVisitRequest> findBySeniorOrderByCreatedAtDesc(User senior);
    List<RoboCompanionVisitRequest> findByStatus(RoboCompanionVisitStatus status);
    boolean existsByAssignedRoboCompanionAndStatusIn(RoboCompanion robot, List<RoboCompanionVisitStatus> statuses);
}
