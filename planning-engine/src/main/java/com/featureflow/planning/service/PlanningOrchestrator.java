package com.featureflow.planning.service;

import com.featureflow.domain.planning.PlanningEngine;
import com.featureflow.domain.planning.PlanningRequest;
import com.featureflow.domain.planning.PlanningResult;
import org.springframework.stereotype.Service;

@Service
public class PlanningOrchestrator {

    private final PlanningEngine planningEngine;

    public PlanningOrchestrator(PlanningEngine planningEngine) {
        this.planningEngine = planningEngine;
    }

    public PlanningResult execute(PlanningRequest request) {
        return planningEngine.plan(request);
    }
}
