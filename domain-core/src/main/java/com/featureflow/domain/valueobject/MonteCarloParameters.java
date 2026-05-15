package com.featureflow.domain.valueobject;

public record MonteCarloParameters(
    int iterations,
    double confidenceLevel
) {
    public MonteCarloParameters {
        if (iterations <= 0) throw new IllegalArgumentException("Iterations must be positive");
        if (confidenceLevel <= 0 || confidenceLevel >= 1) throw new IllegalArgumentException("Confidence level must be in (0, 1)");
    }

    public static MonteCarloParameters defaults() {
        return new MonteCarloParameters(1000, 0.95);
    }
}
