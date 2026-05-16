package com.featureflow.planning.service;

import com.featureflow.domain.entity.*;
import com.featureflow.domain.planning.*;
import com.featureflow.planning.annealing.SimulatedAnnealing;
import com.featureflow.planning.annealing.Solution;
import com.featureflow.planning.greedy.GreedyPlanner;
import com.featureflow.planning.montecarlo.FeatureDeadlineProbability;
import com.featureflow.planning.montecarlo.MonteCarloSimulator;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class PlanningOrchestrator {

    private final GreedyPlanner greedyPlanner;
    private final SimulatedAnnealing annealing;
    private final MonteCarloSimulator monteCarlo;

    public PlanningOrchestrator(
        GreedyPlanner greedyPlanner,
        SimulatedAnnealing annealing,
        MonteCarloSimulator monteCarlo
    ) {
        this.greedyPlanner = greedyPlanner;
        this.annealing = annealing;
        this.monteCarlo = monteCarlo;
    }

    public PlanningResult runFullPipeline(PlanningRequest request) {
        long start = System.currentTimeMillis();

        PlanningResult greedyResult = greedyPlanner.plan(request);

        Solution initialSolution = Solution.from(greedyResult);
        List<UUID> featureIds = request.features().stream()
            .map(FeatureRequest::getId)
            .toList();
        List<UUID> candidateTeams = request.teams().stream()
            .map(Team::getId)
            .toList();

        Solution optimizedSolution = annealing.optimize(
            initialSolution,
            request.parameters().annealing(),
            featureIds,
            candidateTeams,
            new ArrayList<>(request.lockedAssignments())
        );

        Map<UUID, LocalDate> baseDates = new HashMap<>();
        for (var entry : greedyResult.featureTimelines().entrySet()) {
            baseDates.put(entry.getKey(), entry.getValue().endDate());
        }

        Map<UUID, FeatureDeadlineProbability> probabilities = monteCarlo.simulate(
            request.features(),
            baseDates,
            request.parameters().monteCarlo(),
            new Random()
        );

        PlanningResult finalResult = mergeResults(greedyResult, optimizedSolution, probabilities);

        long elapsed = System.currentTimeMillis() - start;

        return new PlanningResult(
            finalResult.assignments(),
            finalResult.conflicts(),
            finalResult.featureTimelines(),
            finalResult.teamLoadReports(),
            finalResult.totalCost(),
            elapsed,
            PlanningResult.PlanningAlgorithm.SIMULATED_ANNEALING
        );
    }

    public List<String> validate(PlanningRequest request) {
        List<String> violations = new ArrayList<>();

        if (request.features().isEmpty()) {
            violations.add("No features in planning request");
        }

        if (request.teams().isEmpty()) {
            violations.add("No teams in planning request");
        }

        for (FeatureRequest feature : request.features()) {
            if (feature.getEffortEstimate().totalHours() <= 0) {
                violations.add("Feature '%s' has zero or negative effort estimate".formatted(feature.getTitle()));
            }

            for (UUID depId : feature.getDependencies()) {
                boolean exists = request.features().stream()
                    .anyMatch(f -> f.getId().equals(depId));
                if (!exists) {
                    violations.add("Feature '%s' depends on non-existent feature %s".formatted(
                        feature.getTitle(), depId));
                }
            }
        }

        for (Team team : request.teams()) {
            if (team.getMembers().isEmpty()) {
                violations.add("Team '%s' has no members".formatted(team.getName()));
            }
        }

        return violations;
    }

    private PlanningResult mergeResults(
        PlanningResult greedyResult,
        Solution optimizedSolution,
        Map<UUID, FeatureDeadlineProbability> probabilities
    ) {
        Map<UUID, FeatureTimeline> updatedTimelines = new LinkedHashMap<>();

        for (var entry : greedyResult.featureTimelines().entrySet()) {
            UUID featureId = entry.getKey();
            FeatureTimeline original = entry.getValue();

            FeatureDeadlineProbability prob = probabilities.get(featureId);
            double probability = prob != null ? prob.probability() : original.probabilityOfMeetingDeadline();

            updatedTimelines.put(featureId, new FeatureTimeline(
                featureId,
                original.startDate(),
                original.endDate(),
                probability
            ));
        }

        return new PlanningResult(
            greedyResult.assignments(),
            greedyResult.conflicts(),
            updatedTimelines,
            greedyResult.teamLoadReports(),
            optimizedSolution.cost(),
            greedyResult.computationTimeMs(),
            PlanningResult.PlanningAlgorithm.SIMULATED_ANNEALING
        );
    }
}
