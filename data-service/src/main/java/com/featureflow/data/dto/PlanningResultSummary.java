package com.featureflow.data.dto;

import java.time.Instant;
import java.util.UUID;

public record PlanningResultSummary(
    UUID id,
    UUID planningWindowId,
    String algorithm,
    double totalCost,
    long computationTimeMs,
    Instant createdAt
) {}
