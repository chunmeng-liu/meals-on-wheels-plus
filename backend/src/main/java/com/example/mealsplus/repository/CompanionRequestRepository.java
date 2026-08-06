package com.example.mealsplus.repository;

import com.example.mealsplus.domain.CompanionRequest;
import com.example.mealsplus.domain.CompanionRequestStatus;
import com.example.mealsplus.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanionRequestRepository extends JpaRepository<CompanionRequest, Long> {
    List<CompanionRequest> findBySeniorOrderByCreatedAtDesc(User senior);
    List<CompanionRequest> findByAssignedVolunteerOrderByScheduledAtAsc(User volunteer);
    List<CompanionRequest> findByStatus(CompanionRequestStatus status);
}
