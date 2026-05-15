package com.featureflow.domain.planning;

import com.featureflow.domain.valueobject.AnnealingParameters;
import com.featureflow.domain.valueobject.MonteCarloParameters;

import java.util.Objects;

public record PlanningParameters(
    double w1Ttm,
    double w2Underutilization,
    double w3DeadlinePenalty,
    int maxParallelFeatures,
    AnnealingParameters annealing,
    MonteCarloParameters monteCarlo
) {
    public PlanningParameters {
        if (w1Ttm < 0 || w2Underutilization < 0 || w3DeadlinePenalty < 0) {
            throw new IllegalArgumentException("Weights must be non-negative");
        }
        if (maxParallelFeatures <= 0) {
            throw new IllegalArgumentException("Max parallel features must be positive");
        }
        Objects.requireNonNull(annealing);
        Objects.requireNonNull(monteCarlo);
    }

    public static PlanningParameters defaults() {
        return new PlanningParameters(
            1.0, 0.5, 2.0, 3,
            AnnealingParameters.defaults(),
            MonteCarloParameters.defaults()
        );
    }
}
