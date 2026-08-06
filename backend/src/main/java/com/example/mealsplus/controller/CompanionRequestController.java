package com.example.mealsplus.controller;

import com.example.mealsplus.dto.ServiceDtos;
import com.example.mealsplus.service.CompanionRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companion-requests")
public class CompanionRequestController {
    private final CompanionRequestService companionRequestService;

    public CompanionRequestController(CompanionRequestService companionRequestService) { this.companionRequestService = companionRequestService; }

    @PreAuthorize("hasRole('SENIOR')")
    @PostMapping
    public ResponseEntity<ServiceDtos.CompanionRequestResponse> create(@Valid @RequestBody ServiceDtos.CompanionRequestCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(companionRequestService.create(request));
    }

    @PreAuthorize("hasRole('SENIOR')")
    @GetMapping("/my")
    public ResponseEntity<List<ServiceDtos.CompanionRequestResponse>> myRequests() {
        return ResponseEntity.ok(companionRequestService.getMyRequests());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<ServiceDtos.CompanionRequestResponse>> listAll() {
        return ResponseEntity.ok(companionRequestService.listAll());
    }

    @PreAuthorize("hasRole('VOLUNTEER')")
    @GetMapping("/assigned")
    public ResponseEntity<List<ServiceDtos.CompanionRequestResponse>> assigned() {
        return ResponseEntity.ok(companionRequestService.getAssignedRequests());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ServiceDtos.CompanionRequestResponse> update(@PathVariable Long id, @Valid @RequestBody ServiceDtos.CompanionRequestUpdateRequest request) {
        return ResponseEntity.ok(companionRequestService.adminUpdate(id, request));
    }

    @PreAuthorize("hasRole('VOLUNTEER')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ServiceDtos.CompanionRequestResponse> updateAssigned(@PathVariable Long id, @Valid @RequestBody ServiceDtos.CompanionRequestUpdateRequest request) {
        return ResponseEntity.ok(companionRequestService.volunteerUpdate(id, request));
    }

    @PreAuthorize("hasRole('SENIOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        companionRequestService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
