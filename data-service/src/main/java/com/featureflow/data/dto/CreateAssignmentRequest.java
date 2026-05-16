package com.featureflow.data.dto;

import java.util.Map;
import java.util.UUID;

public record CreateAssignmentRequest(
    UUID featureId,
    UUID teamId,
    UUID sprintId,
    Map<String, Double> allocatedEffort
) {}
