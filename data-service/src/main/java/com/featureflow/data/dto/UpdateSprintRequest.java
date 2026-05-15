package com.featureflow.data.dto;

import java.time.LocalDate;
import java.util.Map;

public class UpdateSprintRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<String, Object> capacityOverrides;
    private String externalId;

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Map<String, Object> getCapacityOverrides() { return capacityOverrides; }
    public void setCapacityOverrides(Map<String, Object> capacityOverrides) { this.capacityOverrides = capacityOverrides; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
}
