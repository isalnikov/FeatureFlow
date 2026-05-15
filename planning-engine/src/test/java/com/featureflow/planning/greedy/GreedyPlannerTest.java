package com.featureflow.planning.greedy;

import com.featureflow.domain.entity.*;
import com.featureflow.domain.planning.*;
import com.featureflow.domain.valueobject.Capacity;
import com.featureflow.domain.valueobject.EffortEstimate;
import com.featureflow.domain.valueobject.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GreedyPlannerTest {

    private GreedyPlanner planner;

    @BeforeEach
    void setUp() {
        planner = new GreedyPlanner();
    }

    @Test
    void simplePlan_threeFeaturesOneTeam_allAssigned() {
        Team team = team("team-1", "Alpha");
        Product product = product("prod-1", "Product A", team.getId());
        PlanningWindow window = planningWindowWithSprints(3);

        FeatureRequest f1 = feature("f1", 50.0, product.getId(), new EffortEstimate(40, 0, 0, 0));
        FeatureRequest f2 = feature("f2", 30.0, product.getId(), new EffortEstimate(40, 0, 0, 0));
        FeatureRequest f3 = feature("f3", 10.0, product.getId(), new EffortEstimate(40, 0, 0, 0));

        PlanningRequest request = new PlanningRequest(
            List.of(f1, f2, f3),
            List.of(team),
            List.of(product),
            window,
            PlanningParameters.defaults(),
            Set.<UUID>of()
        );

        PlanningResult result = planner.plan(request);

        assertThat(result.assignments()).hasSize(3);
        assertThat(result.conflicts()).isEmpty();
        assertThat(result.featureTimelines()).hasSize(3);
    }

    @Test
    void simplePlan_respectsPriorityOrder_higherValueFirst() {
        Team team = team("team-1", "Alpha");
        Product product = product("prod-1", "Product A", team.getId());
        PlanningWindow window = planningWindowWithSprints(3);

        FeatureRequest lowPriority = feature("low", 10.0, product.getId(), new EffortEstimate(40, 0, 0, 0));
        FeatureRequest highPriority = feature("high", 90.0, product.getId(), new EffortEstimate(40, 0, 0, 0));

        PlanningRequest request = new PlanningRequest(
            List.of(lowPriority, highPriority),
            List.of(team),
            List.of(product),
            window,
            PlanningParameters.defaults(),
            Set.<UUID>of()
        );

        PlanningResult result = planner.plan(request);

        UUID highFeatureSprint = result.assignments().stream()
            .filter(a -> a.getFeatureId().equals(highPriority.getId()))
            .findFirst()
            .orElseThrow()
            .getSprintId();

        UUID lowFeatureSprint = result.assignments().stream()
            .filter(a -> a.getFeatureId().equals(lowPriority.getId()))
            .findFirst()
            .orElseThrow()
            .getSprintId();

        int highIdx = window.getSprints().indexOf(window.getSprints().stream()
            .filter(s -> s.id().equals(highFeatureSprint)).findFirst().orElseThrow());
        int lowIdx = window.getSprints().indexOf(window.getSprints().stream()
            .filter(s -> s.id().equals(lowFeatureSprint)).findFirst().orElseThrow());

        assertThat(highIdx).isLessThanOrEqualTo(lowIdx);
    }

    @Test
    void simplePlan_withDependencies_dependenciesRespected() {
        Team team = team("team-1", "Alpha");
        Product product = product("prod-1", "Product A", team.getId());
        PlanningWindow window = planningWindowWithSprints(4);

        FeatureRequest f1 = feature("f1", 50.0, product.getId(), new EffortEstimate(40, 0, 0, 0));
        FeatureRequest f2 = feature("f2", 50.0, product.getId(), new EffortEstimate(40, 0, 0, 0));
        f2.addDependency(f1.getId());

        PlanningRequest request = new PlanningRequest(
            List.of(f1, f2),
            List.of(team),
            List.of(product),
            window,
            PlanningParameters.defaults(),
            Set.<UUID>of()
        );

        PlanningResult result = planner.plan(request);

        UUID f1Sprint = result.assignments().stream()
            .filter(a -> a.getFeatureId().equals(f1.getId()))
            .findFirst().orElseThrow().getSprintId();
        UUID f2Sprint = result.assignments().stream()
            .filter(a -> a.getFeatureId().equals(f2.getId()))
            .findFirst().orElseThrow().getSprintId();

        List<Sprint> sprints = window.getSprints();
        int f1Idx = indexOf(sprints, f1Sprint);
        int f2Idx = indexOf(sprints, f2Sprint);

        assertThat(f1Idx).isLessThan(f2Idx);
    }

    @Test
    void simplePlan_capacityExceeded_featureDeferredToNextSprint() {
        Team team = team("team-1", "Alpha",
            Map.of(Role.BACKEND, 120.0, Role.FRONTEND, 40.0, Role.QA, 40.0, Role.DEVOPS, 20.0));
        Product product = product("prod-1", "Product A", team.getId());
        PlanningWindow window = planningWindowWithSprints(3);

        FeatureRequest f1 = feature("f1", 50.0, product.getId(), new EffortEstimate(50, 0, 0, 0));
        FeatureRequest f2 = feature("f2", 30.0, product.getId(), new EffortEstimate(50, 0, 0, 0));

        PlanningRequest request = new PlanningRequest(
            List.of(f1, f2),
            List.of(team),
            List.of(product),
            window,
            PlanningParameters.defaults(),
            Set.<UUID>of()
        );

        PlanningResult result = planner.plan(request);

        assertThat(result.assignments()).hasSize(2);

        UUID f1Sprint = result.assignments().stream()
            .filter(a -> a.getFeatureId().equals(f1.getId()))
            .findFirst().orElseThrow().getSprintId();
        UUID f2Sprint = result.assignments().stream()
            .filter(a -> a.getFeatureId().equals(f2.getId()))
            .findFirst().orElseThrow().getSprintId();

        List<Sprint> sprints = window.getSprints();
        int f1Idx = indexOf(sprints, f1Sprint);
        int f2Idx = indexOf(sprints, f2Sprint);

        assertThat(f2Idx).isGreaterThan(f1Idx);
    }

    @Test
    void simplePlan_noCandidateTeam_generatesConflict() {
        Team team = team("team-1", "Alpha");
        Product product = product("prod-1", "Product A", UUID.randomUUID());
        PlanningWindow window = planningWindowWithSprints(2);

        FeatureRequest f1 = feature("f1", 50.0, product.getId(), new EffortEstimate(40, 0, 0, 0));

        PlanningRequest request = new PlanningRequest(
            List.of(f1),
            List.of(team),
            List.of(product),
            window,
            PlanningParameters.defaults(),
            Set.<UUID>of()
        );

        PlanningResult result = planner.plan(request);

        assertThat(result.conflicts()).isNotEmpty();
        assertThat(result.conflicts().get(0).type()).isEqualTo(Conflict.Type.OWNERSHIP_VIOLATION);
    }

    private Team team(String id, String name) {
        return team(id, name, Map.of(Role.BACKEND, 160.0, Role.FRONTEND, 120.0, Role.QA, 80.0, Role.DEVOPS, 40.0));
    }

    private Team team(String id, String name, Map<Role, Double> capacity) {
        Team team = new Team(UUID.fromString("00000000-0000-0000-0000-00000000000" + id.charAt(id.length() - 1)), name);
        team.setDefaultCapacity(new Capacity(capacity, 0.7, 0.20, 0.10));
        return team;
    }

    private Product product(String id, String name, UUID teamId) {
        Product p = new Product(UUID.fromString("00000000-0000-0000-0000-00000000000" + id.charAt(id.length() - 1)), name, "");
        p.addTeamId(teamId);
        return p;
    }

    private FeatureRequest feature(String title, double value, UUID productId, EffortEstimate effort) {
        UUID id = UUID.nameUUIDFromBytes(title.getBytes());
        FeatureRequest f = new FeatureRequest(id, title, "", value);
        f.addProductId(productId);
        f.setEffortEstimate(effort);
        return f;
    }

    private PlanningWindow planningWindowWithSprints(int count) {
        UUID windowId = UUID.randomUUID();
        PlanningWindow window = new PlanningWindow(windowId, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1).plusWeeks(count * 2));
        for (int i = 0; i < count; i++) {
            LocalDate start = LocalDate.of(2025, 1, 1).plusWeeks(i * 2);
            LocalDate end = start.plusWeeks(2);
            window.addSprint(new Sprint(UUID.randomUUID(), start, end));
        }
        return window;
    }

    private int indexOf(List<Sprint> sprints, UUID sprintId) {
        for (int i = 0; i < sprints.size(); i++) {
            if (sprints.get(i).id().equals(sprintId)) return i;
        }
        return -1;
    }
}
