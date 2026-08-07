package com.example.mealsplus.service;

import com.example.mealsplus.domain.*;
import com.example.mealsplus.repository.SeniorProfileRepository;
import com.example.mealsplus.repository.UserRepository;
import com.example.mealsplus.repository.VolunteerProfileRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeedService {
    private final UserRepository userRepository;
    private final SeniorProfileRepository seniorProfileRepository;
    private final VolunteerProfileRepository volunteerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.example.mealsplus.repository.RoboCompanionRepository roboCompanionRepository;

    @Value("${app.demo.enabled:true}") private boolean enabled;
    @Value("${app.demo.admin-email}") private String adminEmail;
    @Value("${app.demo.admin-password}") private String adminPassword;
    @Value("${app.demo.senior-email}") private String seniorEmail;
    @Value("${app.demo.senior-password}") private String seniorPassword;
    @Value("${app.demo.volunteer-email}") private String volunteerEmail;
    @Value("${app.demo.volunteer-password}") private String volunteerPassword;

    public SeedService(UserRepository userRepository, SeniorProfileRepository seniorProfileRepository,
                       VolunteerProfileRepository volunteerProfileRepository, PasswordEncoder passwordEncoder,
                       com.example.mealsplus.repository.RoboCompanionRepository roboCompanionRepository) {
        this.userRepository = userRepository;
        this.seniorProfileRepository = seniorProfileRepository;
        this.volunteerProfileRepository = volunteerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.roboCompanionRepository = roboCompanionRepository;
    }

    @PostConstruct
    @Transactional
    public void seed() {
        if (!enabled) return;
        createIfMissing(adminEmail, adminPassword, "Admin", "User", Role.ADMIN);
        User senior = createIfMissing(seniorEmail, seniorPassword, "Mina", "Senior", Role.SENIOR);
        User volunteer = createIfMissing(volunteerEmail, volunteerPassword, "Alex", "Volunteer", Role.VOLUNTEER);

        seniorProfileRepository.findByUser(senior).orElseGet(() -> {
            SeniorProfile profile = new SeniorProfile();
            profile.setUser(senior);
            profile.setAddress("123 Main Street, Springfield");
            profile.setDietaryNotes("Low sodium");
            profile.setMobilityNotes("Uses a walker");
            profile.setEmergencyContactName("Jane Senior");
            profile.setEmergencyContactPhone("555-0102");
            return seniorProfileRepository.save(profile);
        });
        volunteerProfileRepository.findByUser(volunteer).orElseGet(() -> {
            VolunteerProfile profile = new VolunteerProfile();
            profile.setUser(volunteer);
            profile.setAvailabilityNotes("Weekdays after 10:00 AM");
            return volunteerProfileRepository.save(profile);
        });
        createRobotIfMissing("RC-01", "Stretch Alpha", "Stretch", RoboCompanionStatus.AVAILABLE);
        createRobotIfMissing("RC-02", "Stretch Beta", "Stretch", RoboCompanionStatus.AVAILABLE);
        createRobotIfMissing("RC-03", "Stretch Gamma", "Stretch", RoboCompanionStatus.MAINTENANCE);
    }

    private void createRobotIfMissing(String tag, String name, String model, RoboCompanionStatus status) {
        if (roboCompanionRepository.findByAssetTagIgnoreCase(tag).isPresent()) return;
        RoboCompanion robot = new RoboCompanion(); robot.setAssetTag(tag); robot.setName(name); robot.setModel(model);
        robot.setDescription("Demo physical assistive robot"); robot.setActive(true); robot.setStatus(status);
        roboCompanionRepository.save(robot);
    }

    private User createIfMissing(String email, String password, String firstName, String lastName, Role role) {
        return userRepository.findByEmail(email.toLowerCase()).orElseGet(() -> {
            User user = new User();
            user.setEmail(email.toLowerCase());
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhone("555-0100");
            user.setRole(role);
            user.setActive(true);
            return userRepository.save(user);
        });
    }
}
