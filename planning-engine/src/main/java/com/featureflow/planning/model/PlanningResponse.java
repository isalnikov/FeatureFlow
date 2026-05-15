package com.featureflow.planning.model;

import java.util.UUID;

public record PlanningResponse(
    UUID jobId,
    String status,
    String message,
    long estimatedCompletionMs
) {}
