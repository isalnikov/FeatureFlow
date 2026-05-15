package com.featureflow.data.dto;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public class SprintDto {
    private UUID id;
    private UUID planningWindowId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<String, Object> capacityOverrides;
    private String externalId;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPlanningWindowId() { return planningWindowId; }
    public void setPlanningWindowId(UUID planningWindowId) { this.planningWindowId = planningWindowId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Map<String, Object> getCapacityOverrides() { return capacityOverrides; }
    public void setCapacityOverrides(Map<String, Object> capacityOverrides) { this.capacityOverrides = capacityOverrides; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
}
