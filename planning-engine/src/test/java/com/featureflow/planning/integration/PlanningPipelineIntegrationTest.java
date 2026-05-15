package com.featureflow.planning.integration;

import com.featureflow.domain.entity.*;
import com.featureflow.domain.planning.*;
import com.featureflow.domain.valueobject.*;
import com.featureflow.planning.annealing.Mutator;
import com.featureflow.planning.annealing.SimulatedAnnealing;
import com.featureflow.planning.annealing.Solution;
import com.featureflow.planning.greedy.GreedyPlanner;
import com.featureflow.planning.montecarlo.FeatureDeadlineProbability;
import com.featureflow.planning.montecarlo.MonteCarloSimulator;
import com.featureflow.planning.service.PlanningOrchestrator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the full planning pipeline.
 * Tests real algorithms (no mocks) with realistic data.
 */
class PlanningPipelineIntegrationTest {

    private GreedyPlanner greedyPlanner;
    private SimulatedAnnealing annealing;
    private MonteCarloSimulator monteCarlo;
    private PlanningOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        greedyPlanner = new GreedyPlanner();
        Mutator mutator = new Mutator();
        annealing = new SimulatedAnnealing(mutator);
        monteCarlo = new MonteCarloSimulator();
        orchestrator = new PlanningOrchestrator(greedyPlanner, annealing, monteCarlo);
    }

    @Test
    @DisplayName("Full pipeline: 5 features, 2 teams, produces valid assignments")
    void fullPipeline_fiveFeaturesTwoTeams_validAssignments() {
        Team teamAlpha = createTeam("alpha", Map.of(
            Role.BACKEND, 320.0, Role.FRONTEND, 240.0, Role.QA, 160.0, Role.DEVOPS, 80.0
        ));
        Team teamBeta = createTeam("beta", Map.of(
            Role.BACKEND, 240.0, Role.FRONTEND, 320.0, Role.QA, 120.0, Role.DEVOPS, 80.0
        ));

        Product productA = createProduct("product-a", teamAlpha.getId());
        Product productB = createProduct("product-b", teamBeta.getId());

        PlanningWindow window = createPlanningWindow(6);

        FeatureRequest f1 = createFeature("f1", 100.0, productA.getId(), new EffortEstimate(80, 40, 30, 10));
        FeatureRequest f2 = createFeature("f2", 80.0, productA.getId(), new EffortEstimate(60, 80, 20, 10));
        FeatureRequest f3 = createFeature("f3", 60.0, productB.getId(), new EffortEstimate(40, 100, 40, 20));
        FeatureRequest f4 = createFeature("f4", 40.0, productA.getId(), new EffortEstimate(100, 20, 20, 10));
        FeatureRequest f5 = createFeature("f5", 20.0, productB.getId(), new EffortEstimate(30, 60, 30, 10));

        f2.addDependency(f1.getId());

        PlanningRequest request = new PlanningRequest(
            List.of(f1, f2, f3, f4, f5),
            List.of(teamAlpha, teamBeta),
            List.of(productA, productB),
            window,
            PlanningParameters.defaults(),
            Set.of()
        );

        PlanningResult result = orchestrator.runFullPipeline(request);

        assertThat(result.assignments()).isNotEmpty();
        assertThat(result.totalCost()).isGreaterThan(0);
        assertThat(result.computationTimeMs()).isGreaterThan(0);
        assertThat(result.algorithm()).isEqualTo(PlanningResult.PlanningAlgorithm.SIMULATED_ANNEALING);

        for (Assignment assignment : result.assignments()) {
            assertThat(assignment.getFeatureId()).isNotNull();
            assertThat(assignment.getTeamId()).isNotNull();
            assertThat(assignment.getSprintId()).isNotNull();
        }
    }

    @Test
    @DisplayName("Greedy respects dependencies: dependent feature scheduled after dependency")
    void greedy_respectsDependencies() {
        Team team = createTeam("team-1", Map.of(
            Role.BACKEND, 160.0, Role.FRONTEND, 120.0, Role.QA, 80.0, Role.DEVOPS, 40.0
        ));
        Product product = createProduct("prod-1", team.getId());
        PlanningWindow window = createPlanningWindow(4);

        FeatureRequest parent = createFeature("parent", 50.0, product.getId(), new EffortEstimate(40, 20, 10, 5));
        FeatureRequest child = createFeature("child", 50.0, product.getId(), new EffortEstimate(40, 20, 10, 5));
        child.addDependency(parent.getId());

        PlanningRequest request = new PlanningRequest(
            List.of(parent, child),
            List.of(team),
            List.of(product),
            window,
            PlanningParameters.defaults(),
            Set.of()
        );

        PlanningResult result = greedyPlanner.plan(request);

        UUID parentSprint = findFirstSprint(result, parent.getId());
        UUID childSprint = findFirstSprint(result, child.getId());

        List<Sprint> sprints = window.getSprints();
        int parentIdx = indexOf(sprints, parentSprint);
        int childIdx = indexOf(sprints, childSprint);

        assertThat(childIdx).isGreaterThan(parentIdx);
    }

    @Test
    @DisplayName("Annealing improves or equals greedy cost")
    void annealing_improvesOrEqualsGreedyCost() {
        Team team = createTeam("team-1", Map.of(
            Role.BACKEND, 320.0, Role.FRONTEND, 240.0, Role.QA, 160.0, Role.DEVOPS, 80.0
        ));
        Product product = createProduct("prod-1", team.getId());
        PlanningWindow window = createPlanningWindow(4);

        List<FeatureRequest> features = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            features.add(createFeature("f" + i, 50.0 - i * 5, product.getId(),
                new EffortEstimate(40 + i * 10, 20, 10, 5)));
        }

        PlanningRequest request = new PlanningRequest(
            features,
            List.of(team),
            List.of(product),
            window,
            PlanningParameters.defaults(),
            Set.of()
        );

        PlanningResult greedyResult = greedyPlanner.plan(request);
        Solution initialSolution = Solution.from(greedyResult);

        List<UUID> featureIds = features.stream().map(FeatureRequest::getId).toList();
        List<UUID> candidateTeams = List.of(team.getId());

        AnnealingParameters params = new AnnealingParameters(500.0, 0.95, 0.1, 500);
        Solution optimized = annealing.optimize(initialSolution, params, featureIds, candidateTeams, List.of());

        assertThat(optimized.cost()).isLessThanOrEqualTo(greedyResult.totalCost());
    }

    @Test
    @DisplayName("Monte Carlo produces probabilities in valid range")
    void monteCarlo_validProbabilities() {
        FeatureRequest f1 = createFeature("f1", 50.0, uuid("p1"), new EffortEstimate(40, 20, 10, 5));
        f1.setDeadline(LocalDate.of(2025, 3, 1));
        f1.setStochasticEstimate(new ThreePointEstimate(30, 40, 60));

        FeatureRequest f2 = createFeature("f2", 30.0, uuid("p1"), new EffortEstimate(20, 10, 5, 2));

        MonteCarloParameters params = new MonteCarloParameters(200, 0.95);
        Map<UUID, LocalDate> baseDates = Map.of(
            f1.getId(), LocalDate.of(2025, 2, 1),
            f2.getId(), LocalDate.of(2025, 1, 20)
        );

        Map<UUID, FeatureDeadlineProbability> results = monteCarlo.simulate(
            List.of(f1, f2), baseDates, params, new Random(42)
        );

        assertThat(results).hasSize(2);

        FeatureDeadlineProbability f1Prob = results.get(f1.getId());
        assertThat(f1Prob.probability()).isBetween(0.0, 1.0);
        assertThat(f1Prob.deadline()).isEqualTo(LocalDate.of(2025, 3, 1));
        assertThat(f1Prob.percentile50()).isNotNull();
        assertThat(f1Prob.percentile90()).isNotNull();

        FeatureDeadlineProbability f2Prob = results.get(f2.getId());
        assertThat(f2Prob.probability()).isEqualTo(0.0);
        assertThat(f2Prob.deadline()).isNull();
    }

    @Test
    @DisplayName("Locked assignments are not modified by annealing")
    void annealing_respectsLockedAssignments() {
        Team team = createTeam("team-1", Map.of(
            Role.BACKEND, 160.0, Role.FRONTEND, 120.0, Role.QA, 80.0, Role.DEVOPS, 40.0
        ));
        Product product = createProduct("prod-1", team.getId());
        PlanningWindow window = createPlanningWindow(4);

        FeatureRequest f1 = createFeature("f1", 50.0, product.getId(), new EffortEstimate(40, 20, 10, 5));
        FeatureRequest f2 = createFeature("f2", 30.0, product.getId(), new EffortEstimate(40, 20, 10, 5));

        PlanningRequest request = new PlanningRequest(
            List.of(f1, f2),
            List.of(team),
            List.of(product),
            window,
            PlanningParameters.defaults(),
            Set.of()
        );

        PlanningResult greedyResult = greedyPlanner.plan(request);
        Solution initialSolution = Solution.from(greedyResult);

        List<UUID> featureIds = List.of(f1.getId(), f2.getId());
        List<UUID> candidateTeams = List.of(team.getId());
        List<UUID> lockedFeatures = List.of(f1.getId());

        AnnealingParameters params = new AnnealingParameters(500.0, 0.95, 0.1, 200);
        Solution optimized = annealing.optimize(initialSolution, params, featureIds, candidateTeams, lockedFeatures);

        assertThat(optimized.featureTeamMapping().get(f1.getId()))
            .isEqualTo(initialSolution.featureTeamMapping().get(f1.getId()));
    }

    private Team createTeam(String name, Map<Role, Double> capacity) {
        Team team = new Team(uuid(name), name);
        team.setDefaultCapacity(new Capacity(capacity, 0.7, 0.20, 0.10));
        return team;
    }

    private Product createProduct(String name, UUID teamId) {
        Product p = new Product(uuid(name), name, "");
        p.addTeamId(teamId);
        return p;
    }

    private FeatureRequest createFeature(String title, double value, UUID productId, EffortEstimate effort) {
        UUID id = UUID.nameUUIDFromBytes(title.getBytes());
        FeatureRequest f = new FeatureRequest(id, title, "", value);
        f.addProductId(productId);
        f.setEffortEstimate(effort);
        return f;
    }

    private PlanningWindow createPlanningWindow(int sprintCount) {
        UUID windowId = uuid("window");
        LocalDate start = LocalDate.of(2025, 1, 1);
        PlanningWindow window = new PlanningWindow(windowId, start, start.plusWeeks(sprintCount * 2));
        for (int i = 0; i < sprintCount; i++) {
            LocalDate sprintStart = start.plusWeeks(i * 2);
            LocalDate sprintEnd = sprintStart.plusWeeks(2);
            window.addSprint(new Sprint(uuid("sprint-" + i), sprintStart, sprintEnd));
        }
        return window;
    }

    private UUID findFirstSprint(PlanningResult result, UUID featureId) {
        return result.assignments().stream()
            .filter(a -> a.getFeatureId().equals(featureId))
            .findFirst()
            .orElseThrow()
            .getSprintId();
    }

    private int indexOf(List<Sprint> sprints, UUID sprintId) {
        for (int i = 0; i < sprints.size(); i++) {
            if (sprints.get(i).id().equals(sprintId)) return i;
        }
        return -1;
    }

    private UUID uuid(String suffix) {
        return UUID.nameUUIDFromBytes(suffix.getBytes());
    }
}
