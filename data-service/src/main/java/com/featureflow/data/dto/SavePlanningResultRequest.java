package com.featureflow.data.dto;

import java.time.Instant;
import java.util.UUID;

public record SavePlanningResultRequest(
    UUID planningWindowId,
    String algorithm,
    double totalCost,
    long computationTimeMs,
    String resultData
) {}
