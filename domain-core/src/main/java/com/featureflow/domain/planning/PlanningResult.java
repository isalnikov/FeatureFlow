package com.featureflow.domain.planning;

import com.featureflow.domain.entity.Assignment;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PlanningResult(
    List<Assignment> assignments,
    List<Conflict> conflicts,
    Map<UUID, FeatureTimeline> featureTimelines,
    Map<UUID, TeamLoadReport> teamLoadReports,
    double totalCost,
    long computationTimeMs,
    PlanningAlgorithm algorithm
) {
    public enum PlanningAlgorithm {
        GREEDY,
        SIMULATED_ANNEALING,
        MONTE_CARLO
    }
}
