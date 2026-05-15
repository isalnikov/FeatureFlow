package com.featureflow.domain.planning;

import com.featureflow.domain.entity.*;
import com.featureflow.domain.valueobject.AnnealingParameters;
import com.featureflow.domain.valueobject.MonteCarloParameters;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record PlanningRequest(
    List<FeatureRequest> features,
    List<Team> teams,
    List<Product> products,
    PlanningWindow planningWindow,
    PlanningParameters parameters,
    Set<UUID> lockedAssignments
) {
    public PlanningRequest {
        if (features == null || features.isEmpty()) {
            throw new IllegalArgumentException("Features list cannot be empty");
        }
        if (teams == null || teams.isEmpty()) {
            throw new IllegalArgumentException("Teams list cannot be empty");
        }
    }
}
