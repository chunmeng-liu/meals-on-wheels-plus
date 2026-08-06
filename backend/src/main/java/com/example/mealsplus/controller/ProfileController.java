package com.example.mealsplus.controller;

import com.example.mealsplus.dto.UserDtos;
import com.example.mealsplus.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) { this.profileService = profileService; }

    @GetMapping
    public ResponseEntity<?> getProfile() {
        return ResponseEntity.ok(profileService.getCurrentProfile());
    }

    @PutMapping
    public ResponseEntity<?> updateBasicProfile(@Valid @RequestBody UserDtos.BasicProfileRequest request) {
        return ResponseEntity.ok(profileService.updateBasicProfile(request));
    }

    @PreAuthorize("hasRole('SENIOR')")
    @PutMapping("/senior")
    public ResponseEntity<?> updateSeniorProfile(@Valid @RequestBody UserDtos.SeniorProfileRequest request) {
        return ResponseEntity.ok(profileService.updateSeniorProfile(request));
    }

    @PreAuthorize("hasRole('VOLUNTEER')")
    @PutMapping("/volunteer")
    public ResponseEntity<?> updateVolunteerProfile(@Valid @RequestBody UserDtos.VolunteerProfileRequest request) {
        return ResponseEntity.ok(profileService.updateVolunteerProfile(request));
    }
}
