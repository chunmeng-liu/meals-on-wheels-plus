package com.example.mealsplus;

import com.example.mealsplus.domain.Role;
import com.example.mealsplus.domain.User;
import com.example.mealsplus.dto.AuthDtos;
import com.example.mealsplus.repository.UserRepository;
import com.example.mealsplus.security.JwtService;
import com.example.mealsplus.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    @Test
    void loginReturnsTokenForValidUser() {
        User user = new User();
        user.setEmail("senior@mealsplus.local");
        user.setPasswordHash("hash");
        user.setFirstName("Test");
        user.setLastName("Senior");
        user.setRole(Role.SENIOR);
        user.setActive(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(new UsernamePasswordAuthenticationToken("senior@mealsplus.local", "pw"));
        when(userRepository.findByEmail("senior@mealsplus.local")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("senior@mealsplus.local")).thenReturn("abc");

        AuthDtos.LoginResponse response = authService.login(new AuthDtos.LoginRequest("senior@mealsplus.local", "pw"));

        assertEquals("abc", response.token());
        assertEquals(Role.SENIOR.name(), response.role());
    }
}
