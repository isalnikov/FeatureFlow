package com.featureflow.data.dto;

import java.util.Map;
import java.util.UUID;

public class CreateAssignmentRequest {
    private UUID featureId;
    private UUID teamId;
    private UUID sprintId;
    private Map<String, Double> allocatedEffort;

    public UUID getFeatureId() { return featureId; }
    public void setFeatureId(UUID featureId) { this.featureId = featureId; }
    public UUID getTeamId() { return teamId; }
    public void setTeamId(UUID teamId) { this.teamId = teamId; }
    public UUID getSprintId() { return sprintId; }
    public void setSprintId(UUID sprintId) { this.sprintId = sprintId; }
    public Map<String, Double> getAllocatedEffort() { return allocatedEffort; }
    public void setAllocatedEffort(Map<String, Double> allocatedEffort) { this.allocatedEffort = allocatedEffort; }
}
