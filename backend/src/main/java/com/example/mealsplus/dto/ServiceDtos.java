package com.example.mealsplus.dto;

import com.example.mealsplus.domain.CompanionRequestStatus;
import com.example.mealsplus.domain.MealRequestStatus;
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

    public record DashboardSummary(long totalUsers, long activeUsers, long pendingMealRequests, long pendingCompanionRequests, long completedDeliveries, long completedCompanions) {}
}
