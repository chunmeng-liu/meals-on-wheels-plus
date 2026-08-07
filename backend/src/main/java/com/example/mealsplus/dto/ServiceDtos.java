package com.example.mealsplus.dto;

import com.example.mealsplus.domain.CompanionRequestStatus;
import com.example.mealsplus.domain.MealRequestStatus;
import com.example.mealsplus.domain.RoboCompanionStatus;
import com.example.mealsplus.domain.RoboCompanionVisitStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public class ServiceDtos {
    public record MealRequestCreateRequest(@NotNull @FutureOrPresent LocalDate requestedDeliveryDate,
                                           @NotBlank @Size(max = 100) String mealType,
                                           @NotNull @Min(1) Integer quantity,
                                           @Size(max = 1000) String dietaryNotes,
                                           @NotBlank @Size(max = 500) String deliveryAddress) {}

    public record MealRequestUpdateRequest(MealRequestStatus status, @Size(max = 1000) String adminNotes,
                                           @Size(max = 1000) String completionNotes, Long assignedVolunteerId) {}

    public record MealRequestResponse(Long id, Long seniorId, String seniorName, String seniorPhone,
                                      LocalDate requestedDeliveryDate, String mealType, Integer quantity,
                                      String dietaryNotes, String deliveryAddress, MealRequestStatus status,
                                      Long assignedVolunteerId, String assignedVolunteerName, String adminNotes,
                                      String completionNotes, Instant createdAt, Instant updatedAt) {}

    public record CompanionRequestCreateRequest(@NotNull @FutureOrPresent LocalDate requestedDate,
                                                @NotNull LocalTime requestedTime,
                                                @NotBlank @Size(max = 500) String reason,
                                                @Size(max = 1000) String serviceNotes) {}

    public record CompanionRequestUpdateRequest(CompanionRequestStatus status, Instant scheduledAt,
                                                @Size(max = 1000) String adminNotes,
                                                @Size(max = 1000) String completionNotes,
                                                Long assignedVolunteerId) {}

    public record CompanionRequestResponse(Long id, Long seniorId, String seniorName, String seniorPhone,
                                           String seniorAddress, LocalDate requestedDate, LocalTime requestedTime,
                                           String reason, String serviceNotes, CompanionRequestStatus status,
                                           Instant scheduledAt, Long assignedVolunteerId, String assignedVolunteerName,
                                           String adminNotes, String completionNotes, Instant createdAt, Instant updatedAt) {}

    public record NotificationResponse(Long id, String title, String message, boolean read, Instant createdAt) {}

    public record RoboCompanionRequest(@NotBlank @Size(max = 100) String name, @NotBlank @Size(max = 100) String assetTag,
                                       @NotBlank @Size(max = 100) String model, @Size(max = 1000) String description,
                                       RoboCompanionStatus status, Boolean active, @Size(max = 1000) String notes) {}
    public record RoboCompanionResponse(Long id, String name, String assetTag, String model, String description,
                                        RoboCompanionStatus status, boolean active, String notes, Instant createdAt, Instant updatedAt) {}
    public record RoboVisitCreateRequest(@NotNull @FutureOrPresent LocalDate requestedDate, @NotNull LocalTime requestedTime,
                                         @NotBlank @Size(max = 500) String reason, @Size(max = 1000) String assistanceNeeds,
                                         @Size(max = 1000) String serviceNotes) {}
    public record RoboVisitUpdateRequest(RoboCompanionVisitStatus status, Instant scheduledAt, Long assignedRoboCompanionId,
                                         @Size(max = 1000) String adminNotes, @Size(max = 1000) String completionNotes) {}
    public record RoboVisitResponse(Long id, Long seniorId, String seniorName, String seniorPhone, String seniorAddress,
                                    LocalDate requestedDate, LocalTime requestedTime, String reason, String assistanceNeeds,
                                    String serviceNotes, RoboCompanionVisitStatus status, Instant scheduledAt,
                                    Long assignedRoboCompanionId, String assignedRoboCompanionName, String assignedRoboCompanionModel,
                                    String assignedRoboCompanionAssetTag, String adminNotes, String completionNotes,
                                    Instant createdAt, Instant updatedAt) {}

    public record DashboardSummary(long totalUsers, long activeUsers, long pendingMealRequests, long pendingCompanionRequests,
                                   long completedDeliveries, long completedCompanions, long totalRoboCompanions,
                                   long availableRoboCompanions, long roboCompanionsInService,
                                   long pendingRoboCompanionRequests, long scheduledRoboCompanionVisits) {}
}
