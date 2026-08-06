package com.example.mealsplus.service;

import com.example.mealsplus.domain.Role;
import com.example.mealsplus.domain.SeniorProfile;
import com.example.mealsplus.domain.User;
import com.example.mealsplus.domain.VolunteerProfile;
import com.example.mealsplus.dto.UserDtos;
import com.example.mealsplus.repository.SeniorProfileRepository;
import com.example.mealsplus.repository.UserRepository;
import com.example.mealsplus.repository.VolunteerProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final SeniorProfileRepository seniorProfileRepository;
    private final VolunteerProfileRepository volunteerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, SeniorProfileRepository seniorProfileRepository, VolunteerProfileRepository volunteerProfileRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.seniorProfileRepository = seniorProfileRepository;
        this.volunteerProfileRepository = volunteerProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDtos.UserResponse createUser(UserDtos.CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        user.setRole(request.role());
        user.setActive(request.active() != null ? request.active() : true);
        userRepository.save(user);
        if (request.role() == Role.SENIOR) {
            SeniorProfile profile = new SeniorProfile();
            profile.setUser(user);
            seniorProfileRepository.save(profile);
        }
        if (request.role() == Role.VOLUNTEER) {
            VolunteerProfile profile = new VolunteerProfile();
            profile.setUser(user);
            volunteerProfileRepository.save(profile);
        }
        return toResponse(user);
    }

    public List<UserDtos.UserResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserDtos.UserResponse updateUser(Long id, UserDtos.UpdateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.role() != null) {
            user.setRole(request.role());
            ensureRoleProfile(user, request.role());
        }
        if (request.active() != null) user.setActive(request.active());
        return toResponse(userRepository.save(user));
    }

    public void deactivateUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    private void ensureRoleProfile(User user, Role role) {
        if (role == Role.SENIOR && seniorProfileRepository.findByUser(user).isEmpty()) {
            SeniorProfile profile = new SeniorProfile(); profile.setUser(user); seniorProfileRepository.save(profile);
        }
        if (role == Role.VOLUNTEER && volunteerProfileRepository.findByUser(user).isEmpty()) {
            VolunteerProfile profile = new VolunteerProfile(); profile.setUser(user); volunteerProfileRepository.save(profile);
        }
    }

    private UserDtos.UserResponse toResponse(User user) {
        return new UserDtos.UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhone(), user.getRole(), user.isActive());
    }
}
