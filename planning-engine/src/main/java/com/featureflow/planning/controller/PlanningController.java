package com.featureflow.planning.controller;

import com.featureflow.planning.model.PlanningResponse;
import com.featureflow.planning.model.PlanningRunRequest;
import com.featureflow.planning.service.PlanningOrchestrator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/planning")
@Tag(name = "Planning", description = "Portfolio planning operations")
public class PlanningController {

    private final PlanningOrchestrator orchestrator;

    public PlanningController(PlanningOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/run")
    @Operation(summary = "Run full planning pipeline", description = "Executes Greedy → Simulated Annealing → Monte Carlo")
    public ResponseEntity<PlanningResponse> runPlanning(@RequestBody PlanningRunRequest request) {
        UUID jobId = UUID.randomUUID();

        return ResponseEntity.accepted()
            .header("X-Job-Id", jobId.toString())
            .body(new PlanningResponse(
                jobId,
                "ACCEPTED",
                "Planning job submitted",
                30000
            ));
    }
}
