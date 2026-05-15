package com.featureflow.domain.entity;

import com.featureflow.domain.valueobject.AssignmentStatus;
import com.featureflow.domain.valueobject.EffortEstimate;

import java.util.Objects;
import java.util.UUID;

public final class Assignment {
    private final UUID id;
    private UUID featureId;
    private UUID teamId;
    private UUID sprintId;
    private EffortEstimate allocatedEffort;
    private AssignmentStatus status;

    public Assignment(UUID id, UUID featureId, UUID teamId, UUID sprintId, EffortEstimate allocatedEffort) {
        this.id = Objects.requireNonNull(id);
        this.featureId = Objects.requireNonNull(featureId);
        this.teamId = Objects.requireNonNull(teamId);
        this.sprintId = Objects.requireNonNull(sprintId);
        this.allocatedEffort = Objects.requireNonNull(allocatedEffort);
        this.status = AssignmentStatus.PLANNED;
    }

    public Assignment(UUID id, UUID featureId, UUID teamId, UUID sprintId) {
        this(id, featureId, teamId, sprintId, new EffortEstimate());
    }

    public UUID getId() { return id; }

    public UUID getFeatureId() { return featureId; }
    public void setFeatureId(UUID featureId) { this.featureId = featureId; }

    public UUID getTeamId() { return teamId; }
    public void setTeamId(UUID teamId) { this.teamId = teamId; }

    public UUID getSprintId() { return sprintId; }
    public void setSprintId(UUID sprintId) { this.sprintId = sprintId; }

    public EffortEstimate getAllocatedEffort() { return allocatedEffort; }
    public void setAllocatedEffort(EffortEstimate allocatedEffort) {
        this.allocatedEffort = Objects.requireNonNull(allocatedEffort);
    }

    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = Objects.requireNonNull(status); }

    public void lock() { this.status = AssignmentStatus.LOCKED; }
    public void unlock() { this.status = AssignmentStatus.PLANNED; }
    public boolean isLocked() { return status == AssignmentStatus.LOCKED; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assignment that = (Assignment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Assignment{id=%s, feature=%s, team=%s, sprint=%s, status=%s}".formatted(
            id, featureId, teamId, sprintId, status);
    }
}
