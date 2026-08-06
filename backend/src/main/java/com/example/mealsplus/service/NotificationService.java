package com.example.mealsplus.service;

import com.example.mealsplus.domain.Notification;
import com.example.mealsplus.domain.User;
import com.example.mealsplus.dto.ServiceDtos;
import com.example.mealsplus.repository.NotificationRepository;
import com.example.mealsplus.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<ServiceDtos.NotificationResponse> listCurrentUserNotifications() {
        User user = getCurrentUser();
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream().map(this::toResponse).toList();
    }

    public void markRead(Long id) {
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getUser().getId().equals(getCurrentUser().getId())) throw new IllegalStateException("Not your notification");
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void create(User user, String title, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    private ServiceDtos.NotificationResponse toResponse(Notification notification) {
        return new ServiceDtos.NotificationResponse(notification.getId(), notification.getTitle(), notification.getMessage(), notification.isRead(), notification.getCreatedAt());
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
