package com.example.mealsplus.controller;

import com.example.mealsplus.dto.ServiceDtos;
import com.example.mealsplus.service.RoboCompanionService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/robocompanions") @PreAuthorize("hasRole('ADMIN')")
public class RoboCompanionController {
    private final RoboCompanionService service;
    public RoboCompanionController(RoboCompanionService service){this.service=service;}
    @GetMapping public List<ServiceDtos.RoboCompanionResponse> list(){return service.list();}
    @GetMapping("/available") public List<ServiceDtos.RoboCompanionResponse> available(){return service.available();}
    @GetMapping("/{id}") public ServiceDtos.RoboCompanionResponse get(@PathVariable Long id){return service.get(id);}
    @PostMapping public ResponseEntity<ServiceDtos.RoboCompanionResponse> create(@Valid @RequestBody ServiceDtos.RoboCompanionRequest dto){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));}
    @PutMapping("/{id}") public ServiceDtos.RoboCompanionResponse update(@PathVariable Long id,@Valid @RequestBody ServiceDtos.RoboCompanionRequest dto){return service.update(id,dto);}
}
