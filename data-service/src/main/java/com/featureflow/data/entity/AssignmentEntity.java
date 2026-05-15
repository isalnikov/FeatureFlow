package com.featureflow.data.entity;

import com.featureflow.domain.valueobject.AssignmentStatus;
import org.hibernate.annotations.JdbcTypeCode;
import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "assignments")
public class AssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    private FeatureEntity feature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id", nullable = false)
    private SprintEntity sprint;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allocated_effort", columnDefinition = "jsonb", nullable = false)
    private Map<String, Double> allocatedEffort = Map.of("backendHours", 0.0, "frontendHours", 0.0, "qaHours", 0.0, "devopsHours", 0.0);

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.PLANNED;

    @Version
    private Integer version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public FeatureEntity getFeature() { return feature; }
    public void setFeature(FeatureEntity feature) { this.feature = feature; }
    public TeamEntity getTeam() { return team; }
    public void setTeam(TeamEntity team) { this.team = team; }
    public SprintEntity getSprint() { return sprint; }
    public void setSprint(SprintEntity sprint) { this.sprint = sprint; }
    public Map<String, Double> getAllocatedEffort() { return allocatedEffort; }
    public void setAllocatedEffort(Map<String, Double> allocatedEffort) { this.allocatedEffort = allocatedEffort; }
    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
