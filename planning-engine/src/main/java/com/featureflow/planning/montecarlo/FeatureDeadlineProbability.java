package com.featureflow.planning.montecarlo;

import java.time.LocalDate;
import java.util.UUID;

public record FeatureDeadlineProbability(
    UUID featureId,
    LocalDate deadline,
    double probability,
    LocalDate percentile50,
    LocalDate percentile90
) {}
