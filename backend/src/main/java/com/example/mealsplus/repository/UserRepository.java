package com.example.mealsplus.repository;

import com.example.mealsplus.domain.Role;
import com.example.mealsplus.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    java.util.List<User> findByRole(Role role);
}
