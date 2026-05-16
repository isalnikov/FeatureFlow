package com.featureflow.planning.controller;

import com.featureflow.domain.entity.FeatureRequest;
import com.featureflow.domain.entity.Team;
import com.featureflow.domain.planning.PlanningRequest;
import com.featureflow.domain.planning.PlanningResult;
import com.featureflow.domain.valueobject.AnnealingParameters;
import com.featureflow.domain.valueobject.MonteCarloParameters;
import com.featureflow.domain.planning.PlanningParameters;
import com.featureflow.planning.model.ComparisonResult;
import com.featureflow.planning.model.SimulationChanges;
import com.featureflow.planning.model.SimulationRequest;
import com.featureflow.planning.service.ComparisonService;
import com.featureflow.planning.service.PlanningJobService;
import com.featureflow.planning.service.PlanningOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/simulations")
public class SimulationController {

    private final PlanningOrchestrator orchestrator;
    private final PlanningJobService jobService;

    public SimulationController(PlanningOrchestrator orchestrator, PlanningJobService jobService) {
        this.orchestrator = orchestrator;
        this.jobService = jobService;
    }

    @PostMapping("/run")
    public ResponseEntity<Map<String, String>> runSimulation(@RequestBody SimulationRequest request) {
        PlanningRequest modified = applySimulationChanges(request.baseRequest(), request.changes());
        UUID jobId = jobService.submitJob(modified);
        return ResponseEntity.accepted()
            .header("X-Simulation-Id", jobId.toString())
            .body(Map.of("jobId", jobId.toString(), "status", "RUNNING"));
    }

    @PostMapping("/compare")
    public ResponseEntity<ComparisonResult> compareResults(
        @RequestParam UUID baselineId,
        @RequestParam UUID simulationId
    ) {
        PlanningResult baseline = jobService.getJobResult(baselineId);
        PlanningResult simulation = jobService.getJobResult(simulationId);
        if (baseline == null || simulation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ComparisonService.compare(baseline, simulation));
    }

    private PlanningRequest applySimulationChanges(PlanningRequest base, SimulationChanges changes) {
        List<FeatureRequest> features = new ArrayList<>(base.features());

        if (changes.removedFeatures() != null) {
            Set<UUID> toRemove = new HashSet<>(changes.removedFeatures());
            features.removeIf(f -> toRemove.contains(f.getId()));
        }

        if (changes.addedFeatures() != null) {
            for (UUID featureId : changes.addedFeatures()) {
                var existing = base.features().stream()
                    .filter(f -> f.getId().equals(featureId))
                    .findFirst();
                existing.ifPresent(features::add);
            }
        }

        if (changes.priorityChanges() != null && !changes.priorityChanges().isEmpty()) {
            List<FeatureRequest> updatedFeatures = new ArrayList<>();
            for (FeatureRequest f : features) {
                Double newPriority = changes.priorityChanges().get(f.getId());
                if (newPriority != null) {
                    FeatureRequest updated = new FeatureRequest(
                        f.getId(), f.getTitle(), f.getDescription(), newPriority
                    );
                    updated.setDeadline(f.getDeadline());
                    updated.setClassOfService(f.getClassOfService());
                    updated.setEffortEstimate(f.getEffortEstimate());
                    updated.setStochasticEstimate(f.getStochasticEstimate());
                    updated.setDependencies(f.getDependencies());
                    updated.setRequiredExpertise(f.getRequiredExpertise());
                    updated.setCanSplit(f.isCanSplit());
                    updatedFeatures.add(updated);
                } else {
                    updatedFeatures.add(f);
                }
            }
            features = updatedFeatures;
        }

        List<Team> teams = base.teams();
        if (changes.capacityChanges() != null && !changes.capacityChanges().isEmpty()) {
            teams = teams.stream()
                .map(t -> {
                    Double multiplier = changes.capacityChanges().get(t.getId());
                    if (multiplier != null && t.getDefaultCapacity() != null) {
                        Team copy = new Team(t.getId(), t.getName());
                        copy.setMembers(t.getMembers());
                        copy.setDefaultCapacity(t.getDefaultCapacity().withFocusFactor(
                            t.getDefaultCapacity().focusFactor() * multiplier
                        ));
                        copy.setExpertiseTags(t.getExpertiseTags());
                        copy.setVelocity(t.getVelocity());
                        return copy;
                    }
                    return t;
                })
                .toList();
        }

        return new PlanningRequest(
            features,
            teams,
            base.products(),
            base.planningWindow(),
            base.parameters(),
            base.lockedAssignments()
        );
    }
}
