package com.example.mealsplus.service;

import com.example.mealsplus.domain.MealRequestStatus;
import com.example.mealsplus.domain.CompanionRequestStatus;
import com.example.mealsplus.dto.ServiceDtos;
import com.example.mealsplus.repository.CompanionRequestRepository;
import com.example.mealsplus.repository.MealRequestRepository;
import com.example.mealsplus.repository.UserRepository;
import com.example.mealsplus.repository.RoboCompanionRepository;
import com.example.mealsplus.repository.RoboCompanionVisitRequestRepository;
import com.example.mealsplus.domain.RoboCompanionStatus;
import com.example.mealsplus.domain.RoboCompanionVisitStatus;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final UserRepository userRepository;
    private final MealRequestRepository mealRequestRepository;
    private final CompanionRequestRepository companionRequestRepository;
    private final RoboCompanionRepository roboCompanionRepository;
    private final RoboCompanionVisitRequestRepository roboVisitRepository;

    public DashboardService(UserRepository userRepository, MealRequestRepository mealRequestRepository, CompanionRequestRepository companionRequestRepository,
                            RoboCompanionRepository roboCompanionRepository, RoboCompanionVisitRequestRepository roboVisitRepository) {
        this.userRepository = userRepository;
        this.mealRequestRepository = mealRequestRepository;
        this.companionRequestRepository = companionRequestRepository;
        this.roboCompanionRepository = roboCompanionRepository;
        this.roboVisitRepository = roboVisitRepository;
    }

    public ServiceDtos.DashboardSummary getSummary() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream().filter(u -> u.isActive()).count();
        long pendingMealRequests = mealRequestRepository.findByStatus(MealRequestStatus.REQUESTED).size();
        long pendingCompanionRequests = companionRequestRepository.findByStatus(CompanionRequestStatus.REQUESTED).size();
        long completedDeliveries = mealRequestRepository.findByStatus(MealRequestStatus.DELIVERED).size();
        long completedCompanions = companionRequestRepository.findByStatus(CompanionRequestStatus.COMPLETED).size();
        return new ServiceDtos.DashboardSummary(totalUsers, activeUsers, pendingMealRequests, pendingCompanionRequests, completedDeliveries, completedCompanions,
                roboCompanionRepository.count(), roboCompanionRepository.countByStatus(RoboCompanionStatus.AVAILABLE),
                roboCompanionRepository.countByStatus(RoboCompanionStatus.IN_SERVICE),
                roboVisitRepository.findByStatus(RoboCompanionVisitStatus.REQUESTED).size(),
                roboVisitRepository.findByStatus(RoboCompanionVisitStatus.SCHEDULED).size()+roboVisitRepository.findByStatus(RoboCompanionVisitStatus.ASSIGNED).size());
    }
}
