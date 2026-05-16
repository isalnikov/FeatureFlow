package com.featureflow.planning.model;

import java.time.LocalDate;
import java.util.UUID;

public record TimelineDiff(
    UUID featureId,
    String featureTitle,
    LocalDate baselineStart,
    LocalDate baselineEnd,
    LocalDate simulationStart,
    LocalDate simulationEnd,
    long dayShift
) {}
