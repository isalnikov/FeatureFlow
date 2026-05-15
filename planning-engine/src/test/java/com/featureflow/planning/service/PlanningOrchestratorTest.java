package com.featureflow.planning.service;

import com.featureflow.domain.entity.*;
import com.featureflow.domain.planning.*;
import com.featureflow.domain.valueobject.*;
import com.featureflow.planning.annealing.SimulatedAnnealing;
import com.featureflow.planning.annealing.Solution;
import com.featureflow.planning.greedy.GreedyPlanner;
import com.featureflow.planning.montecarlo.FeatureDeadlineProbability;
import com.featureflow.planning.montecarlo.MonteCarloSimulator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanningOrchestratorTest {

    @Mock
    private GreedyPlanner greedyPlanner;

    @Mock
    private SimulatedAnnealing annealing;

    @Mock
    private MonteCarloSimulator monteCarlo;

    private PlanningOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new PlanningOrchestrator(greedyPlanner, annealing, monteCarlo);
    }

    @Test
    void runFullPipeline_returnsCombinedResult() {
        UUID f1 = uuid("f1");
        UUID t1 = uuid("t1");
        UUID s1 = uuid("s1");

        Assignment assignment = new Assignment(uuid("a1"), f1, t1, s1);
        FeatureTimeline timeline = new FeatureTimeline(f1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 14), 1.0);

        PlanningResult greedyResult = new PlanningResult(
            List.of(assignment),
            List.of(),
            Map.of(f1, timeline),
            Map.of(),
            100.0,
            50,
            PlanningResult.PlanningAlgorithm.GREEDY
        );

        Solution optimizedSolution = new Solution(
            Map.of(f1, assignment),
            Map.of(f1, 0),
            Map.of(f1, t1),
            80.0
        );

        FeatureDeadlineProbability probability = new FeatureDeadlineProbability(
            f1, LocalDate.of(2025, 2, 1), 0.85, LocalDate.of(2025, 1, 12), LocalDate.of(2025, 1, 18)
        );

        when(greedyPlanner.plan(any())).thenReturn(greedyResult);
        when(annealing.optimize(any(), any(), any(), any(), any())).thenReturn(optimizedSolution);
        when(monteCarlo.simulate(any(), any(), any(), any())).thenReturn(Map.of(f1, probability));

        PlanningRequest request = createTestRequest(f1, t1, s1);

        PlanningResult result = orchestrator.runFullPipeline(request);

        assertThat(result.assignments()).hasSize(1);
        assertThat(result.totalCost()).isEqualTo(80.0);
    }

    @Test
    void runFullPipeline_withConflicts_includesConflicts() {
        UUID f1 = uuid("f1");
        UUID t1 = uuid("t1");
        UUID s1 = uuid("s1");

        Conflict conflict = new Conflict(
            Conflict.Type.CAPACITY_EXCEEDED, f1, null, "No capacity"
        );

        PlanningResult greedyResult = new PlanningResult(
            List.of(),
            List.of(conflict),
            Map.of(),
            Map.of(),
            0.0,
            50,
            PlanningResult.PlanningAlgorithm.GREEDY
        );

        Solution optimizedSolution = new Solution(
            Map.of(),
            Map.of(),
            Map.of(),
            0.0
        );

        when(greedyPlanner.plan(any())).thenReturn(greedyResult);
        when(annealing.optimize(any(), any(), any(), any(), any())).thenReturn(optimizedSolution);
        when(monteCarlo.simulate(any(), any(), any(), any())).thenReturn(Map.of());

        PlanningRequest request = createTestRequest(f1, t1, s1);

        PlanningResult result = orchestrator.runFullPipeline(request);

        assertThat(result.conflicts()).hasSize(1);
        assertThat(result.conflicts().get(0).type()).isEqualTo(Conflict.Type.CAPACITY_EXCEEDED);
    }

    @Test
    void runFullPipeline_usesOptimizedCost() {
        UUID f1 = uuid("f1");
        UUID t1 = uuid("t1");
        UUID s1 = uuid("s1");

        Assignment assignment = new Assignment(uuid("a1"), f1, t1, s1);
        FeatureTimeline timeline = new FeatureTimeline(f1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 14), 1.0);

        PlanningResult greedyResult = new PlanningResult(
            List.of(assignment),
            List.of(),
            Map.of(f1, timeline),
            Map.of(),
            200.0,
            50,
            PlanningResult.PlanningAlgorithm.GREEDY
        );

        Solution optimizedSolution = new Solution(
            Map.of(f1, assignment),
            Map.of(f1, 0),
            Map.of(f1, t1),
            150.0
        );

        when(greedyPlanner.plan(any())).thenReturn(greedyResult);
        when(annealing.optimize(any(), any(), any(), any(), any())).thenReturn(optimizedSolution);
        when(monteCarlo.simulate(any(), any(), any(), any())).thenReturn(Map.of());

        PlanningRequest request = createTestRequest(f1, t1, s1);

        PlanningResult result = orchestrator.runFullPipeline(request);

        assertThat(result.totalCost()).isEqualTo(150.0);
    }

    private PlanningRequest createTestRequest(UUID f1, UUID t1, UUID s1) {
        FeatureRequest feature = new FeatureRequest(f1, "f1", "", 50.0);
        feature.setEffortEstimate(new EffortEstimate(40, 0, 0, 0));

        Team team = new Team(t1, "Alpha");
        team.setDefaultCapacity(new Capacity(
            Map.of(Role.BACKEND, 160.0, Role.FRONTEND, 120.0, Role.QA, 80.0, Role.DEVOPS, 40.0),
            0.7, 0.20, 0.10
        ));

        Product product = new Product(uuid("p1"), "Product A", "");
        product.addTeamId(t1);
        feature.addProductId(product.getId());

        PlanningWindow window = new PlanningWindow(uuid("w1"), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        window.addSprint(new Sprint(s1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 14)));

        return new PlanningRequest(
            List.of(feature),
            List.of(team),
            List.of(product),
            window,
            PlanningParameters.defaults(),
            Set.of()
        );
    }

    private UUID uuid(String suffix) {
        return UUID.nameUUIDFromBytes(suffix.getBytes());
    }
}
