package com.featureflow.data.dto;

import java.util.Map;

public class UpdateAssignmentRequest {
    private Map<String, Double> allocatedEffort;

    public Map<String, Double> getAllocatedEffort() { return allocatedEffort; }
    public void setAllocatedEffort(Map<String, Double> allocatedEffort) { this.allocatedEffort = allocatedEffort; }
}
