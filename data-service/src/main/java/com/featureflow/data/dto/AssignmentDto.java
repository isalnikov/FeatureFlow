package com.featureflow.data.dto;

import com.featureflow.domain.valueobject.AssignmentStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AssignmentDto(
    UUID id,
    UUID featureId,
    UUID teamId,
    UUID sprintId,
    Map<String, Double> allocatedEffort,
    AssignmentStatus status,
    Integer version,
    Instant createdAt,
    Instant updatedAt
) {}
