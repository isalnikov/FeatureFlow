package com.featureflow.planning.model;

import java.util.List;
import java.util.UUID;

public record TeamLoadDiff(
    UUID teamId,
    String teamName,
    double baselineUtilization,
    double simulationUtilization,
    double utilizationDelta,
    List<String> newBottleneckRoles
) {}
