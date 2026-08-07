package com.example.mealsplus.service;

import com.example.mealsplus.domain.*;
import com.example.mealsplus.dto.ServiceDtos;
import com.example.mealsplus.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @Transactional
public class RoboCompanionService {
    private final RoboCompanionRepository repository;
    private final RoboCompanionVisitRequestRepository visitRepository;
    public RoboCompanionService(RoboCompanionRepository repository, RoboCompanionVisitRequestRepository visitRepository) {
        this.repository = repository; this.visitRepository = visitRepository;
    }
    @Transactional(readOnly = true) public List<ServiceDtos.RoboCompanionResponse> list() {
        return repository.findAll(Sort.by("name")).stream().map(this::response).toList();
    }
    @Transactional(readOnly = true) public List<ServiceDtos.RoboCompanionResponse> available() {
        return repository.findByActiveTrueAndStatusOrderByName(RoboCompanionStatus.AVAILABLE).stream().map(this::response).toList();
    }
    @Transactional(readOnly = true) public ServiceDtos.RoboCompanionResponse get(Long id) { return response(find(id)); }
    public ServiceDtos.RoboCompanionResponse create(ServiceDtos.RoboCompanionRequest dto) {
        String tag = dto.assetTag().trim();
        if (repository.findByAssetTagIgnoreCase(tag).isPresent()) throw new IllegalArgumentException("Asset tag already exists");
        RoboCompanion robot = new RoboCompanion(); applyEditable(robot, dto); robot.setAssetTag(tag);
        boolean active = dto.active() == null || dto.active();
        RoboCompanionStatus requested = dto.status() == null ? RoboCompanionStatus.AVAILABLE : dto.status();
        if (requested == RoboCompanionStatus.INACTIVE) active = false;
        robot.setActive(active);
        if (!active) requested = RoboCompanionStatus.INACTIVE;
        if (requested == RoboCompanionStatus.ASSIGNED || requested == RoboCompanionStatus.IN_SERVICE)
            throw new IllegalArgumentException("Assignment lifecycle controls that robot status");
        robot.setStatus(requested); return response(repository.save(robot));
    }
    public ServiceDtos.RoboCompanionResponse update(Long id, ServiceDtos.RoboCompanionRequest dto) {
        RoboCompanion robot = repository.findByIdForUpdate(id).orElseThrow(() -> new IllegalArgumentException("RoboCompanion not found"));
        if (!robot.getAssetTag().equalsIgnoreCase(dto.assetTag().trim()) && repository.findByAssetTagIgnoreCase(dto.assetTag().trim()).isPresent())
            throw new IllegalArgumentException("Asset tag already exists");
        boolean busy = robot.getStatus() == RoboCompanionStatus.ASSIGNED || robot.getStatus() == RoboCompanionStatus.IN_SERVICE;
        if (busy && (Boolean.FALSE.equals(dto.active()) || (dto.status() != null && dto.status() != robot.getStatus())))
            throw new IllegalArgumentException("An assigned or in-service RoboCompanion cannot be deactivated or manually changed");
        applyEditable(robot, dto); robot.setAssetTag(dto.assetTag().trim());
        if (dto.active() != null) robot.setActive(dto.active());
        if (!robot.isActive()) robot.setStatus(RoboCompanionStatus.INACTIVE);
        else if (robot.getStatus() == RoboCompanionStatus.INACTIVE) robot.setStatus(RoboCompanionStatus.AVAILABLE);
        if (dto.status() != null && dto.status() != robot.getStatus() && robot.isActive()) {
            if (dto.status() == RoboCompanionStatus.ASSIGNED || dto.status() == RoboCompanionStatus.IN_SERVICE || dto.status() == RoboCompanionStatus.INACTIVE)
                throw new IllegalArgumentException("Use visit assignment or activation controls for that status");
            if (visitRepository.existsByAssignedRoboCompanionAndStatusIn(robot, List.of(RoboCompanionVisitStatus.ASSIGNED, RoboCompanionVisitStatus.IN_PROGRESS)))
                throw new IllegalArgumentException("RoboCompanion has an active visit");
            robot.setStatus(dto.status());
        }
        return response(repository.save(robot));
    }
    private void applyEditable(RoboCompanion robot, ServiceDtos.RoboCompanionRequest dto) {
        robot.setName(dto.name().trim()); robot.setModel(dto.model().trim()); robot.setDescription(dto.description()); robot.setNotes(dto.notes());
    }
    private RoboCompanion find(Long id) { return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("RoboCompanion not found")); }
    ServiceDtos.RoboCompanionResponse response(RoboCompanion r) { return new ServiceDtos.RoboCompanionResponse(r.getId(), r.getName(), r.getAssetTag(), r.getModel(), r.getDescription(), r.getStatus(), r.isActive(), r.getNotes(), r.getCreatedAt(), r.getUpdatedAt()); }
}
