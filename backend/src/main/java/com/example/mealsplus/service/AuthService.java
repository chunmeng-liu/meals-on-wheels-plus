package com.example.mealsplus.service;

import com.example.mealsplus.domain.Role;
import com.example.mealsplus.domain.User;
import com.example.mealsplus.dto.AuthDtos;
import com.example.mealsplus.repository.UserRepository;
import com.example.mealsplus.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userRepository.findByEmail(request.email().toLowerCase()).orElseThrow();
        if (!user.isActive()) throw new IllegalStateException("Account is inactive");
        String token = jwtService.generateToken(user.getEmail());
        return new AuthDtos.LoginResponse(token, user.getRole().name(), user.getEmail(), user.getFirstName(), user.getLastName());
    }

    public Map<String, Object> getCurrentUserSummary() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return Map.of("id", user.getId(), "email", user.getEmail(), "role", user.getRole().name(), "firstName", user.getFirstName(), "lastName", user.getLastName(), "active", user.isActive());
    }

}
