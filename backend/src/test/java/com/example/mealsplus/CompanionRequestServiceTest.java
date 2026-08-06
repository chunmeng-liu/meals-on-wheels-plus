package com.example.mealsplus;

import com.example.mealsplus.domain.CompanionRequest;
import com.example.mealsplus.domain.CompanionRequestStatus;
import com.example.mealsplus.domain.User;
import com.example.mealsplus.dto.ServiceDtos;
import com.example.mealsplus.repository.CompanionRequestRepository;
import com.example.mealsplus.repository.SeniorProfileRepository;
import com.example.mealsplus.repository.UserRepository;
import com.example.mealsplus.service.CompanionRequestService;
import com.example.mealsplus.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanionRequestServiceTest {
    @Mock private CompanionRequestRepository companionRequestRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private SeniorProfileRepository seniorProfileRepository;

    @InjectMocks private CompanionRequestService companionRequestService;

    @Test
    void createCompanionRequestStoresRequestedStatus() {
        User senior = new User();
        senior.setId(1L);
        senior.setEmail("senior@mealsplus.local");
        senior.setFirstName("Test");
        senior.setLastName("Senior");

        Authentication authentication = new UsernamePasswordAuthenticationToken("senior@mealsplus.local", "pw");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(userRepository.findByEmail("senior@mealsplus.local")).thenReturn(Optional.of(senior));
        when(companionRequestRepository.save(org.mockito.ArgumentMatchers.any(CompanionRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(seniorProfileRepository.findByUser(senior)).thenReturn(Optional.empty());

        ServiceDtos.CompanionRequestResponse response = companionRequestService.create(new ServiceDtos.CompanionRequestCreateRequest(LocalDate.now(), LocalTime.NOON, "Need help", ""));

        assertEquals(CompanionRequestStatus.REQUESTED, response.status());
    }

    @Test
    void schedulingRequiresDateAndTime() {
        CompanionRequest request = new CompanionRequest();
        request.setId(12L); request.setStatus(CompanionRequestStatus.APPROVED);
        when(companionRequestRepository.findById(12L)).thenReturn(Optional.of(request));

        assertThrows(IllegalArgumentException.class, () -> companionRequestService.adminUpdate(
                12L, new ServiceDtos.CompanionRequestUpdateRequest(CompanionRequestStatus.SCHEDULED, null, null, null, null)));
    }

    @Test
    void seniorCannotCancelAnotherSeniorsRequest() {
        User owner = user(1L, "owner@local"); User current = user(2L, "other@local");
        CompanionRequest request = new CompanionRequest(); request.setId(12L); request.setSenior(owner); request.setStatus(CompanionRequestStatus.REQUESTED);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(current.getEmail(), "pw"));
        when(userRepository.findByEmail(current.getEmail())).thenReturn(Optional.of(current));
        when(companionRequestRepository.findById(12L)).thenReturn(Optional.of(request));

        assertThrows(IllegalStateException.class, () -> companionRequestService.cancel(12L));
    }

    private User user(Long id, String email) {
        User user = new User(); user.setId(id); user.setEmail(email); user.setFirstName("Test"); user.setLastName("User");
        return user;
    }
}
