package com.example.mealsplus.repository;

import com.example.mealsplus.domain.User;
import com.example.mealsplus.domain.VolunteerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VolunteerProfileRepository extends JpaRepository<VolunteerProfile, Long> {
    Optional<VolunteerProfile> findByUser(User user);
}
