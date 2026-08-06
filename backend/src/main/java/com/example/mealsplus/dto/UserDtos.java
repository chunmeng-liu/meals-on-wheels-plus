package com.example.mealsplus.dto;

import com.example.mealsplus.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserDtos {
    public record CreateUserRequest(@Email @NotBlank String email,
                                    @NotBlank @Size(min = 8, max = 100) String password,
                                    @NotBlank @Size(max = 100) String firstName,
                                    @NotBlank @Size(max = 100) String lastName,
                                    @Size(max = 50) String phone, @NotNull Role role, Boolean active) {}

    public record UpdateUserRequest(@Size(max = 100) String firstName, @Size(max = 100) String lastName,
                                    @Size(max = 50) String phone, Role role, Boolean active) {}

    public record UserResponse(Long id, String email, String firstName, String lastName, String phone, Role role, boolean active) {}

    public record BasicProfileRequest(@NotBlank @Size(max = 100) String firstName,
                                      @NotBlank @Size(max = 100) String lastName,
                                      @Size(max = 50) String phone) {}

    public record SeniorProfileRequest(String address, String dietaryNotes, String mobilityNotes, String emergencyContactName, String emergencyContactPhone) {}

    public record VolunteerProfileRequest(String availabilityNotes) {}
}
