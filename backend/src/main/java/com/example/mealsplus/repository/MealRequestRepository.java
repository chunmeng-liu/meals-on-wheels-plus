package com.example.mealsplus.repository;

import com.example.mealsplus.domain.MealRequest;
import com.example.mealsplus.domain.MealRequestStatus;
import com.example.mealsplus.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealRequestRepository extends JpaRepository<MealRequest, Long> {
    List<MealRequest> findBySeniorOrderByCreatedAtDesc(User senior);
    List<MealRequest> findByAssignedVolunteerOrderByRequestedDeliveryDateAsc(User volunteer);
    List<MealRequest> findByStatus(MealRequestStatus status);
}
