package com.example.mealsplus;

import com.example.mealsplus.domain.*;
import com.example.mealsplus.dto.ServiceDtos;
import com.example.mealsplus.repository.*;
import com.example.mealsplus.service.RoboCompanionVisitService;
import com.example.mealsplus.service.RoboCompanionService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @Transactional
class RoboCompanionVisitServiceTest {
    @Autowired RoboCompanionVisitService service;
    @Autowired RoboCompanionRepository robots;
    @Autowired RoboCompanionVisitRequestRepository visits;
    @Autowired UserRepository users;
    @Autowired NotificationRepository notifications;
    @Autowired RoboCompanionService robotService;
    User senior;
    @BeforeEach void loginSenior(){senior=users.findByEmail("senior@mealsplus.local").orElseThrow();SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(senior.getEmail(),"pw"));}
    @AfterEach void clear(){SecurityContextHolder.clearContext();}
    @Test void seniorCanCreateAndNotificationIsRecorded(){long before=notifications.findByUserOrderByCreatedAtDesc(senior).size();var result=create();assertEquals(RoboCompanionVisitStatus.REQUESTED,result.status());assertEquals(before+1,notifications.findByUserOrderByCreatedAtDesc(senior).size());}
    @Test void seniorCannotReadAnotherSeniorsRequest(){RoboCompanionVisitRequest visit=visits.findById(create().id()).orElseThrow();User other=new User();other.setEmail(UUID.randomUUID()+"@local");other.setPasswordHash("x");other.setFirstName("Other");other.setLastName("Senior");other.setRole(Role.SENIOR);other=users.save(other);visit.setSenior(other);visits.save(visit);assertThrows(IllegalStateException.class,()->service.get(visit.getId()));}
    @Test void lifecycleChangesRobotStatusAndCreatesNotifications(){long before=notifications.findByUserOrderByCreatedAtDesc(senior).size();Long id=create().id();assertThrows(IllegalArgumentException.class,()->service.adminUpdate(id,new ServiceDtos.RoboVisitUpdateRequest(RoboCompanionVisitStatus.ASSIGNED,null,null,null,null)));service.adminUpdate(id,update(RoboCompanionVisitStatus.APPROVED,null,null));service.adminUpdate(id,update(RoboCompanionVisitStatus.SCHEDULED,Instant.now().plus(Duration.ofDays(2)),null));RoboCompanion robot=robot(RoboCompanionStatus.AVAILABLE,true);service.adminUpdate(id,update(RoboCompanionVisitStatus.ASSIGNED,null,robot.getId()));assertEquals(RoboCompanionStatus.ASSIGNED,robots.findById(robot.getId()).orElseThrow().getStatus());service.adminUpdate(id,update(RoboCompanionVisitStatus.IN_PROGRESS,null,null));assertEquals(RoboCompanionStatus.IN_SERVICE,robots.findById(robot.getId()).orElseThrow().getStatus());service.adminUpdate(id,new ServiceDtos.RoboVisitUpdateRequest(RoboCompanionVisitStatus.COMPLETED,null,null,null,"All done"));assertEquals(RoboCompanionStatus.AVAILABLE,robots.findById(robot.getId()).orElseThrow().getStatus());assertEquals(before+6,notifications.findByUserOrderByCreatedAtDesc(senior).size());assertThrows(IllegalArgumentException.class,()->service.cancel(id));}
    @Test void unavailableRobotsCannotBeAssigned(){Long id=scheduled();for(RoboCompanionStatus status:new RoboCompanionStatus[]{RoboCompanionStatus.MAINTENANCE,RoboCompanionStatus.ASSIGNED,RoboCompanionStatus.IN_SERVICE,RoboCompanionStatus.INACTIVE}){RoboCompanion r=robot(status,status!=RoboCompanionStatus.INACTIVE);assertThrows(IllegalArgumentException.class,()->service.adminUpdate(id,update(RoboCompanionVisitStatus.ASSIGNED,null,r.getId())));}}
    @Test void cancellationReleasesAssignedRobot(){Long id=scheduled();RoboCompanion robot=robot(RoboCompanionStatus.AVAILABLE,true);service.adminUpdate(id,update(RoboCompanionVisitStatus.ASSIGNED,null,robot.getId()));service.cancel(id);assertEquals(RoboCompanionStatus.AVAILABLE,robots.findById(robot.getId()).orElseThrow().getStatus());assertEquals(RoboCompanionVisitStatus.CANCELLED,visits.findById(id).orElseThrow().getStatus());}
    @Test void robotCannotBeDoubleAssigned(){Long first=scheduled();Long second=scheduled();RoboCompanion robot=robot(RoboCompanionStatus.AVAILABLE,true);service.adminUpdate(first,update(RoboCompanionVisitStatus.ASSIGNED,null,robot.getId()));assertThrows(IllegalArgumentException.class,()->service.adminUpdate(second,update(RoboCompanionVisitStatus.ASSIGNED,null,robot.getId())));}
    @Test void adminCanCreateMaintainAndDeactivateRobot(){String tag="ADMIN-"+UUID.randomUUID();var created=robotService.create(new ServiceDtos.RoboCompanionRequest("Admin Robot",tag,"Model A","Physical robot",RoboCompanionStatus.AVAILABLE,true,"Ready"));assertEquals(RoboCompanionStatus.AVAILABLE,created.status());var maintenance=robotService.update(created.id(),new ServiceDtos.RoboCompanionRequest("Admin Robot",tag,"Model B","Physical robot",RoboCompanionStatus.MAINTENANCE,true,"Inspection"));assertEquals(RoboCompanionStatus.MAINTENANCE,maintenance.status());var inactive=robotService.update(created.id(),new ServiceDtos.RoboCompanionRequest("Admin Robot",tag,"Model B","Physical robot",RoboCompanionStatus.MAINTENANCE,false,"Stored"));assertFalse(inactive.active());assertEquals(RoboCompanionStatus.INACTIVE,inactive.status());}
    private ServiceDtos.RoboVisitResponse create(){return service.create(new ServiceDtos.RoboVisitCreateRequest(LocalDate.now().plusDays(1),LocalTime.NOON,"Mobility help","Walker assistance","Knock loudly"));}
    private Long scheduled(){Long id=create().id();service.adminUpdate(id,update(RoboCompanionVisitStatus.APPROVED,null,null));service.adminUpdate(id,update(RoboCompanionVisitStatus.SCHEDULED,Instant.now().plus(Duration.ofDays(2)),null));return id;}
    private ServiceDtos.RoboVisitUpdateRequest update(RoboCompanionVisitStatus s,Instant at,Long robot){return new ServiceDtos.RoboVisitUpdateRequest(s,at,robot,null,null);}
    private RoboCompanion robot(RoboCompanionStatus status,boolean active){RoboCompanion r=new RoboCompanion();r.setName("Test Robot");r.setAssetTag("TEST-"+UUID.randomUUID());r.setModel("Test");r.setActive(active);r.setStatus(status);return robots.save(r);}
}
