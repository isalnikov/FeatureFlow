package com.featureflow.planning.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SimulationChanges(
    Map<UUID, Double> priorityChanges,
    Map<UUID, Double> capacityChanges,
    List<UUID> addedFeatures,
    List<UUID> removedFeatures
) {}
