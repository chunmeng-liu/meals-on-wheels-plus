package com.example.mealsplus.service;

import com.example.mealsplus.domain.Role;
import com.example.mealsplus.domain.SeniorProfile;
import com.example.mealsplus.domain.User;
import com.example.mealsplus.domain.VolunteerProfile;
import com.example.mealsplus.dto.UserDtos;
import com.example.mealsplus.repository.SeniorProfileRepository;
import com.example.mealsplus.repository.UserRepository;
import com.example.mealsplus.repository.VolunteerProfileRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final SeniorProfileRepository seniorProfileRepository;
    private final VolunteerProfileRepository volunteerProfileRepository;

    public ProfileService(UserRepository userRepository, SeniorProfileRepository seniorProfileRepository, VolunteerProfileRepository volunteerProfileRepository) {
        this.userRepository = userRepository;
        this.seniorProfileRepository = seniorProfileRepository;
        this.volunteerProfileRepository = volunteerProfileRepository;
    }

    public Map<String, Object> getCurrentProfile() {
        User user = getCurrentUser();
        Map<String, Object> profile = new java.util.HashMap<>();
        profile.put("id", user.getId());
        profile.put("email", user.getEmail());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("phone", user.getPhone());
        profile.put("role", user.getRole().name());
        if (user.getRole() == Role.SENIOR) {
            SeniorProfile seniorProfile = seniorProfileRepository.findByUser(user).orElseGet(() -> createSeniorProfile(user));
            Map<String, Object> details = new java.util.HashMap<>();
            details.put("address", seniorProfile.getAddress());
            details.put("dietaryNotes", seniorProfile.getDietaryNotes());
            details.put("mobilityNotes", seniorProfile.getMobilityNotes());
            details.put("emergencyContactName", seniorProfile.getEmergencyContactName());
            details.put("emergencyContactPhone", seniorProfile.getEmergencyContactPhone());
            profile.put("seniorProfile", details);
        }
        if (user.getRole() == Role.VOLUNTEER) {
            VolunteerProfile volunteerProfile = volunteerProfileRepository.findByUser(user).orElseGet(() -> createVolunteerProfile(user));
            Map<String, Object> details = new java.util.HashMap<>();
            details.put("availabilityNotes", volunteerProfile.getAvailabilityNotes());
            profile.put("volunteerProfile", details);
        }
        return profile;
    }

    public Map<String, Object> updateBasicProfile(UserDtos.BasicProfileRequest request) {
        User user = getCurrentUser();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhone(request.phone());
        userRepository.save(user);
        return getCurrentProfile();
    }

    public Map<String, Object> updateSeniorProfile(UserDtos.SeniorProfileRequest request) {
        User user = getCurrentUser();
        SeniorProfile seniorProfile = seniorProfileRepository.findByUser(user).orElseGet(() -> createSeniorProfile(user));
        if (request.address() != null) seniorProfile.setAddress(request.address());
        if (request.dietaryNotes() != null) seniorProfile.setDietaryNotes(request.dietaryNotes());
        if (request.mobilityNotes() != null) seniorProfile.setMobilityNotes(request.mobilityNotes());
        if (request.emergencyContactName() != null) seniorProfile.setEmergencyContactName(request.emergencyContactName());
        if (request.emergencyContactPhone() != null) seniorProfile.setEmergencyContactPhone(request.emergencyContactPhone());
        seniorProfileRepository.save(seniorProfile);
        return getCurrentProfile();
    }

    public Map<String, Object> updateVolunteerProfile(UserDtos.VolunteerProfileRequest request) {
        User user = getCurrentUser();
        VolunteerProfile volunteerProfile = volunteerProfileRepository.findByUser(user).orElseGet(() -> createVolunteerProfile(user));
        if (request.availabilityNotes() != null) volunteerProfile.setAvailabilityNotes(request.availabilityNotes());
        volunteerProfileRepository.save(volunteerProfile);
        return getCurrentProfile();
    }

    private SeniorProfile createSeniorProfile(User user) {
        SeniorProfile profile = new SeniorProfile();
        profile.setUser(user);
        return seniorProfileRepository.save(profile);
    }

    private VolunteerProfile createVolunteerProfile(User user) {
        VolunteerProfile profile = new VolunteerProfile();
        profile.setUser(user);
        return volunteerProfileRepository.save(profile);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
