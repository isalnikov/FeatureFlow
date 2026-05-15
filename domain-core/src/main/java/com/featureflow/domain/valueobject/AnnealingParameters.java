package com.featureflow.domain.valueobject;

public record AnnealingParameters(
    double initialTemperature,
    double coolingRate,
    double minTemperature,
    int maxIterations
) {
    public AnnealingParameters {
        if (initialTemperature <= 0) throw new IllegalArgumentException("Initial temperature must be positive");
        if (coolingRate <= 0 || coolingRate >= 1) throw new IllegalArgumentException("Cooling rate must be in (0, 1)");
        if (minTemperature <= 0) throw new IllegalArgumentException("Min temperature must be positive");
        if (maxIterations <= 0) throw new IllegalArgumentException("Max iterations must be positive");
    }

    public static AnnealingParameters defaults() {
        return new AnnealingParameters(1000.0, 0.95, 0.1, 10000);
    }
}
