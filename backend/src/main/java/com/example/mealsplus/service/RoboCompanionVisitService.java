package com.example.mealsplus.service;

import com.example.mealsplus.domain.*;
import com.example.mealsplus.dto.ServiceDtos;
import com.example.mealsplus.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service @Transactional
public class RoboCompanionVisitService {
    private static final Map<RoboCompanionVisitStatus, Set<RoboCompanionVisitStatus>> TRANSITIONS = Map.of(
        RoboCompanionVisitStatus.REQUESTED, EnumSet.of(RoboCompanionVisitStatus.APPROVED, RoboCompanionVisitStatus.REJECTED, RoboCompanionVisitStatus.CANCELLED),
        RoboCompanionVisitStatus.APPROVED, EnumSet.of(RoboCompanionVisitStatus.SCHEDULED, RoboCompanionVisitStatus.CANCELLED),
        RoboCompanionVisitStatus.SCHEDULED, EnumSet.of(RoboCompanionVisitStatus.ASSIGNED, RoboCompanionVisitStatus.CANCELLED),
        RoboCompanionVisitStatus.ASSIGNED, EnumSet.of(RoboCompanionVisitStatus.IN_PROGRESS, RoboCompanionVisitStatus.CANCELLED),
        RoboCompanionVisitStatus.IN_PROGRESS, EnumSet.of(RoboCompanionVisitStatus.COMPLETED));
    private final RoboCompanionVisitRequestRepository repository;
    private final RoboCompanionRepository robotRepository;
    private final UserRepository userRepository;
    private final SeniorProfileRepository seniorProfileRepository;
    private final NotificationService notifications;
    public RoboCompanionVisitService(RoboCompanionVisitRequestRepository repository, RoboCompanionRepository robotRepository,
            UserRepository userRepository, SeniorProfileRepository seniorProfileRepository, NotificationService notifications) {
        this.repository=repository; this.robotRepository=robotRepository; this.userRepository=userRepository;
        this.seniorProfileRepository=seniorProfileRepository; this.notifications=notifications;
    }
    public ServiceDtos.RoboVisitResponse create(ServiceDtos.RoboVisitCreateRequest dto) {
        if (!LocalDateTime.of(dto.requestedDate(), dto.requestedTime()).isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Requested date and time must be in the future");
        User senior=currentUser(); RoboCompanionVisitRequest item=new RoboCompanionVisitRequest(); item.setSenior(senior);
        item.setRequestedDate(dto.requestedDate()); item.setRequestedTime(dto.requestedTime()); item.setReason(dto.reason().trim());
        item.setAssistanceNeeds(dto.assistanceNeeds()); item.setServiceNotes(dto.serviceNotes()); item=repository.save(item);
        notifications.create(senior,"RoboCompanion Visit submitted","Your RoboCompanion Visit request is awaiting review."); return response(item);
    }
    @Transactional(readOnly=true) public List<ServiceDtos.RoboVisitResponse> my() { return repository.findBySeniorOrderByCreatedAtDesc(currentUser()).stream().map(this::response).toList(); }
    @Transactional(readOnly=true) public List<ServiceDtos.RoboVisitResponse> all(RoboCompanionVisitStatus status) {
        List<RoboCompanionVisitRequest> items=status==null?repository.findAll(Sort.by(Sort.Direction.DESC,"createdAt")):repository.findByStatus(status);
        return items.stream().map(this::response).toList();
    }
    @Transactional(readOnly=true) public ServiceDtos.RoboVisitResponse get(Long id) {
        RoboCompanionVisitRequest item=find(id); User user=currentUser();
        if (user.getRole()!=Role.ADMIN && !item.getSenior().getId().equals(user.getId())) throw new IllegalStateException("You can only view your own RoboCompanion visits");
        return response(item);
    }
    public ServiceDtos.RoboVisitResponse adminUpdate(Long id, ServiceDtos.RoboVisitUpdateRequest dto) {
        RoboCompanionVisitRequest item=find(id);
        if (dto.adminNotes()!=null) item.setAdminNotes(dto.adminNotes());
        if (dto.completionNotes()!=null) item.setCompletionNotes(dto.completionNotes());
        if (dto.scheduledAt()!=null) {
            if (!dto.scheduledAt().isAfter(Instant.now())) throw new IllegalArgumentException("Scheduled time must be in the future");
            item.setScheduledAt(dto.scheduledAt());
        }
        if (dto.assignedRoboCompanionId()!=null) assign(item,dto.assignedRoboCompanionId());
        if (dto.status()!=null && dto.status()!=item.getStatus()) transition(item,dto.status());
        return response(repository.save(item));
    }
    public void cancel(Long id) {
        RoboCompanionVisitRequest item=find(id); User senior=currentUser();
        if (!item.getSenior().getId().equals(senior.getId())) throw new IllegalStateException("You can only cancel your own requests");
        if (!EnumSet.of(RoboCompanionVisitStatus.REQUESTED,RoboCompanionVisitStatus.APPROVED,RoboCompanionVisitStatus.SCHEDULED,RoboCompanionVisitStatus.ASSIGNED).contains(item.getStatus()))
            throw new IllegalArgumentException("This RoboCompanion Visit can no longer be cancelled");
        release(item); item.setStatus(RoboCompanionVisitStatus.CANCELLED); repository.save(item);
        notifications.create(senior,"RoboCompanion Visit cancelled","Your RoboCompanion Visit was cancelled.");
    }
    private void assign(RoboCompanionVisitRequest item, Long robotId) {
        if (item.getStatus()!=RoboCompanionVisitStatus.SCHEDULED) throw new IllegalArgumentException("Visit must be scheduled before assigning a RoboCompanion");
        RoboCompanion robot=robotRepository.findByIdForUpdate(robotId).orElseThrow(()->new IllegalArgumentException("RoboCompanion not found"));
        if (!robot.isActive() || robot.getStatus()!=RoboCompanionStatus.AVAILABLE) throw new IllegalArgumentException("RoboCompanion must be active and AVAILABLE");
        if (repository.existsByAssignedRoboCompanionAndStatusIn(robot,List.of(RoboCompanionVisitStatus.ASSIGNED,RoboCompanionVisitStatus.IN_PROGRESS)))
            throw new IllegalArgumentException("RoboCompanion already has an active assignment");
        item.setAssignedRoboCompanion(robot); robot.setStatus(RoboCompanionStatus.ASSIGNED); robotRepository.save(robot);
    }
    private void transition(RoboCompanionVisitRequest item, RoboCompanionVisitStatus to) {
        RoboCompanionVisitStatus from=item.getStatus();
        if (!TRANSITIONS.getOrDefault(from,Set.of()).contains(to)) throw new IllegalArgumentException("Invalid RoboCompanion Visit status transition: "+from+" to "+to);
        if (to==RoboCompanionVisitStatus.SCHEDULED && item.getScheduledAt()==null) throw new IllegalArgumentException("A date and time are required before scheduling");
        if (to==RoboCompanionVisitStatus.ASSIGNED && item.getAssignedRoboCompanion()==null) throw new IllegalArgumentException("Assign an available RoboCompanion first");
        if (to==RoboCompanionVisitStatus.REJECTED || to==RoboCompanionVisitStatus.CANCELLED) release(item);
        if (to==RoboCompanionVisitStatus.IN_PROGRESS) { requireRobot(item,RoboCompanionStatus.ASSIGNED); item.getAssignedRoboCompanion().setStatus(RoboCompanionStatus.IN_SERVICE); }
        if (to==RoboCompanionVisitStatus.COMPLETED) { requireRobot(item,RoboCompanionStatus.IN_SERVICE); item.getAssignedRoboCompanion().setStatus(RoboCompanionStatus.AVAILABLE); }
        item.setStatus(to); notify(item,to);
    }
    private void requireRobot(RoboCompanionVisitRequest item,RoboCompanionStatus status) {
        if(item.getAssignedRoboCompanion()==null || item.getAssignedRoboCompanion().getStatus()!=status) throw new IllegalArgumentException("Assigned RoboCompanion is not "+status);
    }
    private void release(RoboCompanionVisitRequest item) { RoboCompanion r=item.getAssignedRoboCompanion(); if(r!=null && r.getStatus()==RoboCompanionStatus.ASSIGNED) r.setStatus(RoboCompanionStatus.AVAILABLE); }
    private void notify(RoboCompanionVisitRequest item,RoboCompanionVisitStatus status) {
        String message=switch(status) {
            case APPROVED -> "Your RoboCompanion Visit was approved.";
            case REJECTED -> "Your RoboCompanion Visit was not approved. Review the admin notes for details.";
            case SCHEDULED -> "Your RoboCompanion Visit has been scheduled for "+DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a").withZone(ZoneId.systemDefault()).format(item.getScheduledAt())+".";
            case ASSIGNED -> "RoboCompanion "+item.getAssignedRoboCompanion().getAssetTag()+" has been assigned to your visit.";
            case IN_PROGRESS -> "Your RoboCompanion Visit is now in progress.";
            case COMPLETED -> "Your RoboCompanion Visit was completed.";
            case CANCELLED -> "Your RoboCompanion Visit was cancelled.";
            default -> null; };
        if(message!=null) notifications.create(item.getSenior(),"RoboCompanion Visit update",message);
    }
    private RoboCompanionVisitRequest find(Long id){return repository.findById(id).orElseThrow(()->new IllegalArgumentException("RoboCompanion Visit request not found"));}
    private User currentUser(){String email=SecurityContextHolder.getContext().getAuthentication().getName();return userRepository.findByEmail(email).orElseThrow(()->new IllegalArgumentException("User not found"));}
    private String name(User u){return u.getFirstName()+" "+u.getLastName();}
    private ServiceDtos.RoboVisitResponse response(RoboCompanionVisitRequest v){RoboCompanion r=v.getAssignedRoboCompanion();String address=seniorProfileRepository.findByUser(v.getSenior()).map(SeniorProfile::getAddress).orElse(null);return new ServiceDtos.RoboVisitResponse(v.getId(),v.getSenior().getId(),name(v.getSenior()),v.getSenior().getPhone(),address,v.getRequestedDate(),v.getRequestedTime(),v.getReason(),v.getAssistanceNeeds(),v.getServiceNotes(),v.getStatus(),v.getScheduledAt(),r==null?null:r.getId(),r==null?null:r.getName(),r==null?null:r.getModel(),r==null?null:r.getAssetTag(),v.getAdminNotes(),v.getCompletionNotes(),v.getCreatedAt(),v.getUpdatedAt());}
}
