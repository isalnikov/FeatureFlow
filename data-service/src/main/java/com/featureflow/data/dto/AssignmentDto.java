package com.featureflow.data.dto;

import com.featureflow.domain.valueobject.AssignmentStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class AssignmentDto {
    private UUID id;
    private UUID featureId;
    private UUID teamId;
    private UUID sprintId;
    private Map<String, Double> allocatedEffort;
    private AssignmentStatus status;
    private Integer version;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getFeatureId() { return featureId; }
    public void setFeatureId(UUID featureId) { this.featureId = featureId; }
    public UUID getTeamId() { return teamId; }
    public void setTeamId(UUID teamId) { this.teamId = teamId; }
    public UUID getSprintId() { return sprintId; }
    public void setSprintId(UUID sprintId) { this.sprintId = sprintId; }
    public Map<String, Double> getAllocatedEffort() { return allocatedEffort; }
    public void setAllocatedEffort(Map<String, Double> allocatedEffort) { this.allocatedEffort = allocatedEffort; }
    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
