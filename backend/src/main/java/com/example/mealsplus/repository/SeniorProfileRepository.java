package com.example.mealsplus.repository;

import com.example.mealsplus.domain.SeniorProfile;
import com.example.mealsplus.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeniorProfileRepository extends JpaRepository<SeniorProfile, Long> {
    Optional<SeniorProfile> findByUser(User user);
}
