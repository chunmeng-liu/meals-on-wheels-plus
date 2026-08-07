package com.example.mealsplus.domain;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "robocompanion_visit_requests")
public class RoboCompanionVisitRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "senior_id", nullable = false) private User senior;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "robo_companion_id") private RoboCompanion assignedRoboCompanion;
    @Column(nullable = false) private LocalDate requestedDate;
    @Column(nullable = false) private LocalTime requestedTime;
    @Column(nullable = false, length = 500) private String reason;
    @Column(length = 1000) private String assistanceNeeds;
    @Column(length = 1000) private String serviceNotes;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private RoboCompanionVisitStatus status = RoboCompanionVisitStatus.REQUESTED;
    private Instant scheduledAt;
    @Column(length = 1000) private String adminNotes;
    @Column(length = 1000) private String completionNotes;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    @Column(nullable = false) private Instant updatedAt = Instant.now();
    @PreUpdate public void preUpdate() { updatedAt = Instant.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public User getSenior() { return senior; } public void setSenior(User senior) { this.senior = senior; }
    public RoboCompanion getAssignedRoboCompanion() { return assignedRoboCompanion; } public void setAssignedRoboCompanion(RoboCompanion value) { this.assignedRoboCompanion = value; }
    public LocalDate getRequestedDate() { return requestedDate; } public void setRequestedDate(LocalDate value) { this.requestedDate = value; }
    public LocalTime getRequestedTime() { return requestedTime; } public void setRequestedTime(LocalTime value) { this.requestedTime = value; }
    public String getReason() { return reason; } public void setReason(String value) { this.reason = value; }
    public String getAssistanceNeeds() { return assistanceNeeds; } public void setAssistanceNeeds(String value) { this.assistanceNeeds = value; }
    public String getServiceNotes() { return serviceNotes; } public void setServiceNotes(String value) { this.serviceNotes = value; }
    public RoboCompanionVisitStatus getStatus() { return status; } public void setStatus(RoboCompanionVisitStatus value) { this.status = value; }
    public Instant getScheduledAt() { return scheduledAt; } public void setScheduledAt(Instant value) { this.scheduledAt = value; }
    public String getAdminNotes() { return adminNotes; } public void setAdminNotes(String value) { this.adminNotes = value; }
    public String getCompletionNotes() { return completionNotes; } public void setCompletionNotes(String value) { this.completionNotes = value; }
    public Instant getCreatedAt() { return createdAt; } public Instant getUpdatedAt() { return updatedAt; }
}
