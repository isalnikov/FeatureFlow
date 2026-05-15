package com.featureflow.planning.model;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record PlanningRunRequest(
    List<UUID> featureIds,
    List<UUID> teamIds,
    UUID planningWindowId,
    PlanningParametersDto parameters,
    Set<UUID> lockedAssignmentIds
) {
    public record PlanningParametersDto(
        double w1Ttm,
        double w2Underutilization,
        double w3DeadlinePenalty,
        int maxParallelFeatures,
        AnnealingParamsDto annealing,
        MonteCarloParamsDto monteCarlo
    ) {}

    public record AnnealingParamsDto(
        double initialTemperature,
        double coolingRate,
        double minTemperature,
        int maxIterations
    ) {}

    public record MonteCarloParamsDto(
        int iterations,
        double confidenceLevel
    ) {}
}
