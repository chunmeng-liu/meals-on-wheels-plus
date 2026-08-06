package com.example.mealsplus;

import com.example.mealsplus.domain.MealRequest;
import com.example.mealsplus.domain.MealRequestStatus;
import com.example.mealsplus.domain.User;
import com.example.mealsplus.dto.ServiceDtos;
import com.example.mealsplus.repository.MealRequestRepository;
import com.example.mealsplus.repository.UserRepository;
import com.example.mealsplus.service.MealRequestService;
import com.example.mealsplus.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MealRequestServiceTest {
    @Mock private MealRequestRepository mealRequestRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private MealRequestService mealRequestService;

    @Test
    void createMealRequestStoresRequestedStatus() {
        User senior = new User();
        senior.setId(1L);
        senior.setEmail("senior@mealsplus.local");
        senior.setFirstName("Test");
        senior.setLastName("Senior");

        Authentication authentication = new UsernamePasswordAuthenticationToken("senior@mealsplus.local", "pw");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(userRepository.findByEmail("senior@mealsplus.local")).thenReturn(Optional.of(senior));
        when(mealRequestRepository.save(org.mockito.ArgumentMatchers.any(MealRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceDtos.MealRequestResponse response = mealRequestService.create(new ServiceDtos.MealRequestCreateRequest(LocalDate.now(), "Soup", 1, "", "123 Main"));

        assertEquals(MealRequestStatus.REQUESTED, response.status());
    }

    @Test
    void rejectsInvalidMealStatusJump() {
        MealRequest meal = new MealRequest();
        meal.setId(10L);
        meal.setStatus(MealRequestStatus.REQUESTED);
        when(mealRequestRepository.findById(10L)).thenReturn(Optional.of(meal));

        assertThrows(IllegalArgumentException.class, () -> mealRequestService.adminUpdate(
                10L, new ServiceDtos.MealRequestUpdateRequest(MealRequestStatus.DELIVERED, null, null, null)));
    }

    @Test
    void volunteerCannotUpdateSomeoneElsesMeal() {
        User assigned = user(2L, "assigned@local");
        User current = user(3L, "current@local");
        MealRequest meal = new MealRequest();
        meal.setId(10L); meal.setStatus(MealRequestStatus.ASSIGNED); meal.setAssignedVolunteer(assigned);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(current.getEmail(), "pw"));
        when(userRepository.findByEmail(current.getEmail())).thenReturn(Optional.of(current));
        when(mealRequestRepository.findById(10L)).thenReturn(Optional.of(meal));

        assertThrows(IllegalStateException.class, () -> mealRequestService.volunteerUpdate(
                10L, new ServiceDtos.MealRequestUpdateRequest(MealRequestStatus.PREPARING, null, null, null)));
    }

    private User user(Long id, String email) {
        User user = new User(); user.setId(id); user.setEmail(email); user.setFirstName("Test"); user.setLastName("Volunteer");
        return user;
    }
}
