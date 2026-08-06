package com.example.mealsplus.controller;

import com.example.mealsplus.dto.ServiceDtos;
import com.example.mealsplus.service.MealRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meal-requests")
public class MealRequestController {
    private final MealRequestService mealRequestService;

    public MealRequestController(MealRequestService mealRequestService) { this.mealRequestService = mealRequestService; }

    @PreAuthorize("hasRole('SENIOR')")
    @PostMapping
    public ResponseEntity<ServiceDtos.MealRequestResponse> create(@Valid @RequestBody ServiceDtos.MealRequestCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mealRequestService.create(request));
    }

    @PreAuthorize("hasRole('SENIOR')")
    @GetMapping("/my")
    public ResponseEntity<List<ServiceDtos.MealRequestResponse>> myRequests() {
        return ResponseEntity.ok(mealRequestService.getMyRequests());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<ServiceDtos.MealRequestResponse>> listAll() {
        return ResponseEntity.ok(mealRequestService.listAll());
    }

    @PreAuthorize("hasRole('VOLUNTEER')")
    @GetMapping("/assigned")
    public ResponseEntity<List<ServiceDtos.MealRequestResponse>> assigned() {
        return ResponseEntity.ok(mealRequestService.getAssignedRequests());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ServiceDtos.MealRequestResponse> update(@PathVariable Long id, @Valid @RequestBody ServiceDtos.MealRequestUpdateRequest request) {
        return ResponseEntity.ok(mealRequestService.adminUpdate(id, request));
    }

    @PreAuthorize("hasRole('VOLUNTEER')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ServiceDtos.MealRequestResponse> updateAssigned(@PathVariable Long id, @Valid @RequestBody ServiceDtos.MealRequestUpdateRequest request) {
        return ResponseEntity.ok(mealRequestService.volunteerUpdate(id, request));
    }

    @PreAuthorize("hasRole('SENIOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        mealRequestService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
