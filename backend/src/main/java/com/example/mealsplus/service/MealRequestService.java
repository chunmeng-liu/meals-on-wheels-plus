package com.example.mealsplus.service;

import com.example.mealsplus.domain.*;
import com.example.mealsplus.dto.ServiceDtos;
import com.example.mealsplus.repository.MealRequestRepository;
import com.example.mealsplus.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class MealRequestService {
    private static final Map<MealRequestStatus, Set<MealRequestStatus>> TRANSITIONS = Map.of(
            MealRequestStatus.REQUESTED, EnumSet.of(MealRequestStatus.APPROVED, MealRequestStatus.REJECTED, MealRequestStatus.CANCELLED),
            MealRequestStatus.APPROVED, EnumSet.of(MealRequestStatus.ASSIGNED, MealRequestStatus.CANCELLED),
            MealRequestStatus.ASSIGNED, EnumSet.of(MealRequestStatus.PREPARING, MealRequestStatus.CANCELLED),
            MealRequestStatus.PREPARING, EnumSet.of(MealRequestStatus.OUT_FOR_DELIVERY),
            MealRequestStatus.OUT_FOR_DELIVERY, EnumSet.of(MealRequestStatus.DELIVERED)
    );

    private final MealRequestRepository mealRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public MealRequestService(MealRequestRepository mealRequestRepository, UserRepository userRepository,
                              NotificationService notificationService) {
        this.mealRequestRepository = mealRequestRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public ServiceDtos.MealRequestResponse create(ServiceDtos.MealRequestCreateRequest request) {
        User senior = getCurrentUser();
        MealRequest mealRequest = new MealRequest();
        mealRequest.setSenior(senior);
        mealRequest.setRequestedDeliveryDate(request.requestedDeliveryDate());
        mealRequest.setMealType(request.mealType().trim());
        mealRequest.setQuantity(request.quantity());
        mealRequest.setDietaryNotes(request.dietaryNotes());
        mealRequest.setDeliveryAddress(request.deliveryAddress().trim());
        MealRequest saved = mealRequestRepository.save(mealRequest);
        notificationService.create(senior, "Meal request submitted", "Your meal request is awaiting review.");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ServiceDtos.MealRequestResponse> getMyRequests() {
        return mealRequestRepository.findBySeniorOrderByCreatedAtDesc(getCurrentUser()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceDtos.MealRequestResponse> listAll() {
        return mealRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceDtos.MealRequestResponse> getAssignedRequests() {
        return mealRequestRepository.findByAssignedVolunteerOrderByRequestedDeliveryDateAsc(getCurrentUser()).stream().map(this::toResponse).toList();
    }

    public ServiceDtos.MealRequestResponse adminUpdate(Long id, ServiceDtos.MealRequestUpdateRequest request) {
        MealRequest meal = find(id);
        if (request.assignedVolunteerId() != null) meal.setAssignedVolunteer(requireActiveVolunteer(request.assignedVolunteerId()));
        if (request.status() != null && request.status() != meal.getStatus()) {
            validateTransition(meal.getStatus(), request.status());
            if (request.status() == MealRequestStatus.ASSIGNED && meal.getAssignedVolunteer() == null) {
                throw new IllegalArgumentException("Assign an active volunteer before marking the meal assigned");
            }
            meal.setStatus(request.status());
            notifySenior(meal, request.status());
            if (request.status() == MealRequestStatus.ASSIGNED) {
                notificationService.create(meal.getAssignedVolunteer(), "New meal assignment", "A meal delivery has been assigned to you.");
            }
        }
        if (request.adminNotes() != null) meal.setAdminNotes(request.adminNotes());
        if (request.completionNotes() != null) meal.setCompletionNotes(request.completionNotes());
        return toResponse(mealRequestRepository.save(meal));
    }

    public ServiceDtos.MealRequestResponse volunteerUpdate(Long id, ServiceDtos.MealRequestUpdateRequest request) {
        MealRequest meal = find(id);
        User volunteer = getCurrentUser();
        if (meal.getAssignedVolunteer() == null || !meal.getAssignedVolunteer().getId().equals(volunteer.getId())) {
            throw new IllegalStateException("This meal delivery is not assigned to you");
        }
        if (request.status() == null) throw new IllegalArgumentException("Status is required");
        Set<MealRequestStatus> volunteerStatuses = EnumSet.of(MealRequestStatus.PREPARING, MealRequestStatus.OUT_FOR_DELIVERY, MealRequestStatus.DELIVERED);
        if (!volunteerStatuses.contains(request.status())) throw new IllegalArgumentException("Volunteers cannot set that status");
        validateTransition(meal.getStatus(), request.status());
        meal.setStatus(request.status());
        if (request.completionNotes() != null) meal.setCompletionNotes(request.completionNotes());
        notifySenior(meal, request.status());
        return toResponse(mealRequestRepository.save(meal));
    }

    public void cancel(Long id) {
        MealRequest meal = find(id);
        User senior = getCurrentUser();
        if (!meal.getSenior().getId().equals(senior.getId())) throw new IllegalStateException("You can only cancel your own requests");
        if (!EnumSet.of(MealRequestStatus.REQUESTED, MealRequestStatus.APPROVED, MealRequestStatus.ASSIGNED).contains(meal.getStatus())) {
            throw new IllegalArgumentException("This delivery can no longer be cancelled");
        }
        meal.setStatus(MealRequestStatus.CANCELLED);
        mealRequestRepository.save(meal);
    }

    private MealRequest find(Long id) {
        return mealRequestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Meal request not found"));
    }

    private User requireActiveVolunteer(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Volunteer not found"));
        if (user.getRole() != Role.VOLUNTEER || !user.isActive()) throw new IllegalArgumentException("Assignment requires an active volunteer");
        return user;
    }

    private void validateTransition(MealRequestStatus from, MealRequestStatus to) {
        if (!TRANSITIONS.getOrDefault(from, Set.of()).contains(to)) {
            throw new IllegalArgumentException("Invalid meal status transition: " + from + " to " + to);
        }
    }

    private void notifySenior(MealRequest meal, MealRequestStatus status) {
        String message = switch (status) {
            case APPROVED -> "Your meal request was approved.";
            case REJECTED -> "Your meal request was not approved. Review the admin notes for details.";
            case ASSIGNED -> "A volunteer was assigned to your meal delivery.";
            case PREPARING -> "Your meal is being prepared.";
            case OUT_FOR_DELIVERY -> "Your meal is out for delivery.";
            case DELIVERED -> "Your meal delivery was completed.";
            default -> null;
        };
        if (message != null) notificationService.create(meal.getSenior(), "Meal update", message);
    }

    private ServiceDtos.MealRequestResponse toResponse(MealRequest request) {
        return new ServiceDtos.MealRequestResponse(
                request.getId(), request.getSenior().getId(), fullName(request.getSenior()), request.getSenior().getPhone(),
                request.getRequestedDeliveryDate(), request.getMealType(), request.getQuantity(), request.getDietaryNotes(),
                request.getDeliveryAddress(), request.getStatus(),
                request.getAssignedVolunteer() == null ? null : request.getAssignedVolunteer().getId(),
                request.getAssignedVolunteer() == null ? null : fullName(request.getAssignedVolunteer()),
                request.getAdminNotes(), request.getCompletionNotes(), request.getCreatedAt(), request.getUpdatedAt());
    }

    private String fullName(User user) { return user.getFirstName() + " " + user.getLastName(); }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
