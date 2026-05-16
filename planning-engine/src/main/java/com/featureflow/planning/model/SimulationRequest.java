package com.featureflow.planning.model;

import com.featureflow.domain.planning.PlanningRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SimulationRequest(
    PlanningRequest baseRequest,
    SimulationChanges changes
) {}
