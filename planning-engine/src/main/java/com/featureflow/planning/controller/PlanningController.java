package com.featureflow.planning.controller;

import com.featureflow.domain.planning.PlanningRequest;
import com.featureflow.domain.planning.PlanningResult;
import com.featureflow.planning.model.PlanningResponse;
import com.featureflow.planning.model.PlanningRunRequest;
import com.featureflow.planning.service.PlanningJobService;
import com.featureflow.planning.service.PlanningOrchestrator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/planning")
@Tag(name = "Planning", description = "Portfolio planning operations")
public class PlanningController {

    private final PlanningOrchestrator orchestrator;
    private final PlanningJobService jobService;

    public PlanningController(PlanningOrchestrator orchestrator, PlanningJobService jobService) {
        this.orchestrator = orchestrator;
        this.jobService = jobService;
    }

    @PostMapping("/run")
    @Operation(summary = "Run full planning pipeline asynchronously", description = "Submits planning job and returns jobId for status tracking")
    public ResponseEntity<PlanningResponse> runPlanning(@RequestBody PlanningRequest request) {
        UUID jobId = jobService.submitJob(request);

        return ResponseEntity.accepted()
            .header("X-Job-Id", jobId.toString())
            .body(new PlanningResponse(
                jobId,
                "ACCEPTED",
                "Planning job submitted. Check status via GET /jobs/{jobId}/status",
                30000
            ));
    }

    @PostMapping("/execute")
    @Operation(summary = "Execute planning synchronously", description = "Runs full pipeline and returns result immediately")
    public ResponseEntity<PlanningResult> executePlanning(@RequestBody PlanningRequest request) {
        PlanningResult result = orchestrator.runFullPipeline(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/jobs/{jobId}/status")
    @Operation(summary = "Get planning job status")
    public ResponseEntity<PlanningJobService.PlanningJob> getJobStatus(@PathVariable UUID jobId) {
        try {
            PlanningJobService.PlanningJob job = jobService.getJobStatus(jobId);
            return ResponseEntity.ok(job);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/jobs/{jobId}/result")
    @Operation(summary = "Get planning job result")
    public ResponseEntity<PlanningResult> getResult(@PathVariable UUID jobId) {
        PlanningResult result = jobService.getJobResult(jobId);
        return result != null
            ? ResponseEntity.ok(result)
            : ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/jobs/{jobId}/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream planning job status via SSE")
    public SseEmitter streamStatus(@PathVariable UUID jobId) {
        try {
            jobService.getJobStatus(jobId);
            return jobService.streamStatus(jobId);
        } catch (IllegalArgumentException e) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new RuntimeException("Job not found: " + jobId));
            return emitter;
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate planning request", description = "Checks constraints without executing planning")
    public ResponseEntity<List<String>> validate(@RequestBody PlanningRunRequest request) {
        PlanningRequest planningRequest = toPlanningRequest(request);
        List<String> violations = orchestrator.validate(planningRequest);
        return violations.isEmpty()
            ? ResponseEntity.ok(List.of("OK"))
            : ResponseEntity.badRequest().body(violations);
    }

    private PlanningRequest toPlanningRequest(PlanningRunRequest request) {
        com.featureflow.domain.valueobject.AnnealingParameters annealing =
            request.parameters() != null && request.parameters().annealing() != null
                ? new com.featureflow.domain.valueobject.AnnealingParameters(
                    request.parameters().annealing().initialTemperature(),
                    request.parameters().annealing().coolingRate(),
                    request.parameters().annealing().minTemperature(),
                    request.parameters().annealing().maxIterations())
                : com.featureflow.domain.valueobject.AnnealingParameters.defaults();

        com.featureflow.domain.valueobject.MonteCarloParameters monteCarlo =
            request.parameters() != null && request.parameters().monteCarlo() != null
                ? new com.featureflow.domain.valueobject.MonteCarloParameters(
                    request.parameters().monteCarlo().iterations(),
                    request.parameters().monteCarlo().confidenceLevel())
                : com.featureflow.domain.valueobject.MonteCarloParameters.defaults();

        com.featureflow.domain.planning.PlanningParameters params =
            request.parameters() != null
                ? new com.featureflow.domain.planning.PlanningParameters(
                    request.parameters().w1Ttm(),
                    request.parameters().w2Underutilization(),
                    request.parameters().w3DeadlinePenalty(),
                    request.parameters().maxParallelFeatures(),
                    annealing,
                    monteCarlo)
                : com.featureflow.domain.planning.PlanningParameters.defaults();

        com.featureflow.domain.entity.PlanningWindow window =
            new com.featureflow.domain.entity.PlanningWindow(
                UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusMonths(6));
        window.addSprint(new com.featureflow.domain.entity.Sprint(
            UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusWeeks(2)));

        com.featureflow.domain.entity.Team dummyTeam =
            new com.featureflow.domain.entity.Team(UUID.randomUUID(), "dummy");
        dummyTeam.setDefaultCapacity(new com.featureflow.domain.valueobject.Capacity(
            Map.of(com.featureflow.domain.valueobject.Role.BACKEND, 160.0,
                   com.featureflow.domain.valueobject.Role.FRONTEND, 120.0,
                   com.featureflow.domain.valueobject.Role.QA, 80.0,
                   com.featureflow.domain.valueobject.Role.DEVOPS, 40.0),
            0.7, 0.20, 0.10));

        com.featureflow.domain.entity.Product dummyProduct =
            new com.featureflow.domain.entity.Product(UUID.randomUUID(), "dummy", "");
        dummyProduct.addTeamId(dummyTeam.getId());

        com.featureflow.domain.entity.FeatureRequest dummyFeature =
            new com.featureflow.domain.entity.FeatureRequest(UUID.randomUUID(), "dummy", "", 1.0);
        dummyFeature.setEffortEstimate(new com.featureflow.domain.valueobject.EffortEstimate(1, 0, 0, 0));
        dummyFeature.addProductId(dummyProduct.getId());

        return new PlanningRequest(
            List.of(dummyFeature),
            List.of(dummyTeam),
            List.of(dummyProduct),
            window,
            params,
            request.lockedAssignmentIds() != null ? request.lockedAssignmentIds() : Set.of()
        );
    }
}
