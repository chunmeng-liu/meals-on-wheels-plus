package com.example.mealsplus.controller;

import com.example.mealsplus.domain.RoboCompanionVisitStatus;
import com.example.mealsplus.dto.ServiceDtos;
import com.example.mealsplus.service.RoboCompanionVisitService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/robocompanion-requests")
public class RoboCompanionVisitController {
    private final RoboCompanionVisitService service;
    public RoboCompanionVisitController(RoboCompanionVisitService service){this.service=service;}
    @PreAuthorize("hasRole('SENIOR')") @PostMapping public ResponseEntity<ServiceDtos.RoboVisitResponse> create(@Valid @RequestBody ServiceDtos.RoboVisitCreateRequest dto){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));}
    @PreAuthorize("hasRole('SENIOR')") @GetMapping("/my") public List<ServiceDtos.RoboVisitResponse> my(){return service.my();}
    @PreAuthorize("hasAnyRole('SENIOR','ADMIN')") @GetMapping("/{id}") public ServiceDtos.RoboVisitResponse get(@PathVariable Long id){return service.get(id);}
    @PreAuthorize("hasRole('ADMIN')") @GetMapping public List<ServiceDtos.RoboVisitResponse> all(@RequestParam(required=false) RoboCompanionVisitStatus status){return service.all(status);}
    @PreAuthorize("hasRole('ADMIN')") @PutMapping("/{id}") public ServiceDtos.RoboVisitResponse update(@PathVariable Long id,@Valid @RequestBody ServiceDtos.RoboVisitUpdateRequest dto){return service.adminUpdate(id,dto);}
    @PreAuthorize("hasRole('SENIOR')") @DeleteMapping("/{id}") public ResponseEntity<Void> cancel(@PathVariable Long id){service.cancel(id);return ResponseEntity.noContent().build();}
}
