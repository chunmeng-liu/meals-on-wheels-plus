package com.example.mealsplus.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "robo_companions", uniqueConstraints = @UniqueConstraint(name = "uk_robo_asset_tag", columnNames = "asset_tag"))
public class RoboCompanion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String name;
    @Column(name = "asset_tag", nullable = false, length = 100) private String assetTag;
    @Column(nullable = false, length = 100) private String model;
    @Column(length = 1000) private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private RoboCompanionStatus status = RoboCompanionStatus.AVAILABLE;
    @Column(nullable = false) private boolean active = true;
    @Column(length = 1000) private String notes;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    @Column(nullable = false) private Instant updatedAt = Instant.now();
    @PreUpdate public void preUpdate() { updatedAt = Instant.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getAssetTag() { return assetTag; } public void setAssetTag(String assetTag) { this.assetTag = assetTag; }
    public String getModel() { return model; } public void setModel(String model) { this.model = model; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public RoboCompanionStatus getStatus() { return status; } public void setStatus(RoboCompanionStatus status) { this.status = status; }
    public boolean isActive() { return active; } public void setActive(boolean active) { this.active = active; }
    public String getNotes() { return notes; } public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; } public Instant getUpdatedAt() { return updatedAt; }
}
