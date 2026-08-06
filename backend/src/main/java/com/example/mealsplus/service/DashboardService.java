package com.example.mealsplus.service;

import com.example.mealsplus.domain.MealRequestStatus;
import com.example.mealsplus.domain.CompanionRequestStatus;
import com.example.mealsplus.dto.ServiceDtos;
import com.example.mealsplus.repository.CompanionRequestRepository;
import com.example.mealsplus.repository.MealRequestRepository;
import com.example.mealsplus.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final UserRepository userRepository;
    private final MealRequestRepository mealRequestRepository;
    private final CompanionRequestRepository companionRequestRepository;

    public DashboardService(UserRepository userRepository, MealRequestRepository mealRequestRepository, CompanionRequestRepository companionRequestRepository) {
        this.userRepository = userRepository;
        this.mealRequestRepository = mealRequestRepository;
        this.companionRequestRepository = companionRequestRepository;
    }

    public ServiceDtos.DashboardSummary getSummary() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream().filter(u -> u.isActive()).count();
        long pendingMealRequests = mealRequestRepository.findByStatus(MealRequestStatus.REQUESTED).size();
        long pendingCompanionRequests = companionRequestRepository.findByStatus(CompanionRequestStatus.REQUESTED).size();
        long completedDeliveries = mealRequestRepository.findByStatus(MealRequestStatus.DELIVERED).size();
        long completedCompanions = companionRequestRepository.findByStatus(CompanionRequestStatus.COMPLETED).size();
        return new ServiceDtos.DashboardSummary(totalUsers, activeUsers, pendingMealRequests, pendingCompanionRequests, completedDeliveries, completedCompanions);
    }
}
