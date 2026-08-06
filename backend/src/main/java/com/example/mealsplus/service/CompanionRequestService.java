package com.example.mealsplus.service;

import com.example.mealsplus.domain.*;
import com.example.mealsplus.dto.ServiceDtos;
import com.example.mealsplus.repository.CompanionRequestRepository;
import com.example.mealsplus.repository.SeniorProfileRepository;
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
public class CompanionRequestService {
    private static final Map<CompanionRequestStatus, Set<CompanionRequestStatus>> TRANSITIONS = Map.of(
            CompanionRequestStatus.REQUESTED, EnumSet.of(CompanionRequestStatus.APPROVED, CompanionRequestStatus.REJECTED, CompanionRequestStatus.CANCELLED),
            CompanionRequestStatus.APPROVED, EnumSet.of(CompanionRequestStatus.SCHEDULED, CompanionRequestStatus.CANCELLED),
            CompanionRequestStatus.SCHEDULED, EnumSet.of(CompanionRequestStatus.ASSIGNED, CompanionRequestStatus.CANCELLED),
            CompanionRequestStatus.ASSIGNED, EnumSet.of(CompanionRequestStatus.IN_PROGRESS, CompanionRequestStatus.CANCELLED),
            CompanionRequestStatus.IN_PROGRESS, EnumSet.of(CompanionRequestStatus.COMPLETED)
    );

    private final CompanionRequestRepository companionRequestRepository;
    private final UserRepository userRepository;
    private final SeniorProfileRepository seniorProfileRepository;
    private final NotificationService notificationService;

    public CompanionRequestService(CompanionRequestRepository companionRequestRepository, UserRepository userRepository,
                                   NotificationService notificationService, SeniorProfileRepository seniorProfileRepository) {
        this.companionRequestRepository = companionRequestRepository;
        this.userRepository = userRepository;
        this.seniorProfileRepository = seniorProfileRepository;
        this.notificationService = notificationService;
    }

    public ServiceDtos.CompanionRequestResponse create(ServiceDtos.CompanionRequestCreateRequest request) {
        User senior = getCurrentUser();
        CompanionRequest item = new CompanionRequest();
        item.setSenior(senior);
        item.setRequestedDate(request.requestedDate());
        item.setRequestedTime(request.requestedTime());
        item.setReason(request.reason().trim());
        item.setServiceNotes(request.serviceNotes());
        CompanionRequest saved = companionRequestRepository.save(item);
        notificationService.create(senior, "Companion request submitted", "Your companion request is awaiting review.");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ServiceDtos.CompanionRequestResponse> getMyRequests() {
        return companionRequestRepository.findBySeniorOrderByCreatedAtDesc(getCurrentUser()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceDtos.CompanionRequestResponse> listAll() {
        return companionRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceDtos.CompanionRequestResponse> getAssignedRequests() {
        return companionRequestRepository.findByAssignedVolunteerOrderByScheduledAtAsc(getCurrentUser()).stream().map(this::toResponse).toList();
    }

    public ServiceDtos.CompanionRequestResponse adminUpdate(Long id, ServiceDtos.CompanionRequestUpdateRequest request) {
        CompanionRequest item = find(id);
        if (request.scheduledAt() != null) item.setScheduledAt(request.scheduledAt());
        if (request.assignedVolunteerId() != null) item.setAssignedVolunteer(requireActiveVolunteer(request.assignedVolunteerId()));
        if (request.status() != null && request.status() != item.getStatus()) {
            validateTransition(item.getStatus(), request.status());
            if (request.status() == CompanionRequestStatus.SCHEDULED && item.getScheduledAt() == null) {
                throw new IllegalArgumentException("A date and time are required before scheduling");
            }
            if (request.status() == CompanionRequestStatus.ASSIGNED && item.getAssignedVolunteer() == null) {
                throw new IllegalArgumentException("Assign an active volunteer before marking the service assigned");
            }
            item.setStatus(request.status());
            notifySenior(item, request.status());
            if (request.status() == CompanionRequestStatus.ASSIGNED) {
                notificationService.create(item.getAssignedVolunteer(), "New companion assignment", "A companion service has been assigned to you.");
            }
        }
        if (request.adminNotes() != null) item.setAdminNotes(request.adminNotes());
        if (request.completionNotes() != null) item.setCompletionNotes(request.completionNotes());
        return toResponse(companionRequestRepository.save(item));
    }

    public ServiceDtos.CompanionRequestResponse volunteerUpdate(Long id, ServiceDtos.CompanionRequestUpdateRequest request) {
        CompanionRequest item = find(id);
        User volunteer = getCurrentUser();
        if (item.getAssignedVolunteer() == null || !item.getAssignedVolunteer().getId().equals(volunteer.getId())) {
            throw new IllegalStateException("This companion service is not assigned to you");
        }
        if (request.status() == null) throw new IllegalArgumentException("Status is required");
        if (!EnumSet.of(CompanionRequestStatus.IN_PROGRESS, CompanionRequestStatus.COMPLETED).contains(request.status())) {
            throw new IllegalArgumentException("Volunteers cannot set that status");
        }
        validateTransition(item.getStatus(), request.status());
        item.setStatus(request.status());
        if (request.completionNotes() != null) item.setCompletionNotes(request.completionNotes());
        notifySenior(item, request.status());
        return toResponse(companionRequestRepository.save(item));
    }

    public void cancel(Long id) {
        CompanionRequest item = find(id);
        User senior = getCurrentUser();
        if (!item.getSenior().getId().equals(senior.getId())) throw new IllegalStateException("You can only cancel your own requests");
        if (!EnumSet.of(CompanionRequestStatus.REQUESTED, CompanionRequestStatus.APPROVED,
                CompanionRequestStatus.SCHEDULED, CompanionRequestStatus.ASSIGNED).contains(item.getStatus())) {
            throw new IllegalArgumentException("This service can no longer be cancelled");
        }
        item.setStatus(CompanionRequestStatus.CANCELLED);
        companionRequestRepository.save(item);
    }

    private CompanionRequest find(Long id) {
        return companionRequestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Companion request not found"));
    }

    private User requireActiveVolunteer(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Volunteer not found"));
        if (user.getRole() != Role.VOLUNTEER || !user.isActive()) throw new IllegalArgumentException("Assignment requires an active volunteer");
        return user;
    }

    private void validateTransition(CompanionRequestStatus from, CompanionRequestStatus to) {
        if (!TRANSITIONS.getOrDefault(from, Set.of()).contains(to)) {
            throw new IllegalArgumentException("Invalid companion status transition: " + from + " to " + to);
        }
    }

    private void notifySenior(CompanionRequest item, CompanionRequestStatus status) {
        String message = switch (status) {
            case APPROVED -> "Your companion request was approved.";
            case REJECTED -> "Your companion request was not approved. Review the admin notes for details.";
            case SCHEDULED -> "Your companion service was scheduled.";
            case ASSIGNED -> "A volunteer was assigned to your companion service.";
            case IN_PROGRESS -> "Your companion service is now in progress.";
            case COMPLETED -> "Your companion service was completed.";
            default -> null;
        };
        if (message != null) notificationService.create(item.getSenior(), "Companion service update", message);
    }

    private ServiceDtos.CompanionRequestResponse toResponse(CompanionRequest request) {
        String address = seniorProfileRepository.findByUser(request.getSenior()).map(SeniorProfile::getAddress).orElse(null);
        return new ServiceDtos.CompanionRequestResponse(
                request.getId(), request.getSenior().getId(), fullName(request.getSenior()), request.getSenior().getPhone(), address,
                request.getRequestedDate(), request.getRequestedTime(), request.getReason(), request.getServiceNotes(), request.getStatus(),
                request.getScheduledAt(), request.getAssignedVolunteer() == null ? null : request.getAssignedVolunteer().getId(),
                request.getAssignedVolunteer() == null ? null : fullName(request.getAssignedVolunteer()), request.getAdminNotes(),
                request.getCompletionNotes(), request.getCreatedAt(), request.getUpdatedAt());
    }

    private String fullName(User user) { return user.getFirstName() + " " + user.getLastName(); }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
