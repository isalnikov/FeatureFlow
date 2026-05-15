package com.featureflow.domain.planning;

import java.util.concurrent.CompletableFuture;

public interface PlanningEngine {

    PlanningResult plan(PlanningRequest request);

    CompletableFuture<PlanningResult> planAsync(PlanningRequest request);
}
