package com.example.mealsplus.controller;

import com.example.mealsplus.dto.ServiceDtos;
import com.example.mealsplus.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) { this.notificationService = notificationService; }

    @GetMapping
    public ResponseEntity<List<ServiceDtos.NotificationResponse>> list() {
        return ResponseEntity.ok(notificationService.listCurrentUserNotifications());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.noContent().build();
    }
}
