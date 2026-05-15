package com.featureflow.planning.controller;

import com.featureflow.domain.planning.PlanningRequest;
import com.featureflow.domain.planning.PlanningResult;
import com.featureflow.planning.service.PlanningJobService;
import com.featureflow.planning.service.PlanningOrchestrator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/planning")
public class PlanningController {

    private final PlanningOrchestrator orchestrator;
    private final PlanningJobService jobService;

    public PlanningController(PlanningOrchestrator orchestrator, PlanningJobService jobService) {
        this.orchestrator = orchestrator;
        this.jobService = jobService;
    }

    @PostMapping("/execute")
    public PlanningResult execute(@RequestBody PlanningRequest request) {
        return orchestrator.execute(request);
    }

    @PostMapping("/execute/async")
    public UUID executeAsync(@RequestBody PlanningRequest request) {
        return jobService.submitJob(request);
    }

    @GetMapping("/status/{jobId}")
    public PlanningJobService.PlanningJob getJobStatus(@PathVariable UUID jobId) {
        return jobService.getJobStatus(jobId);
    }
}
