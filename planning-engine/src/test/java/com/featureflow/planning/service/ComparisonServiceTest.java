package com.featureflow.planning.service;

import com.featureflow.domain.planning.Conflict;
import com.featureflow.domain.planning.FeatureTimeline;
import com.featureflow.domain.planning.PlanningResult;
import com.featureflow.domain.planning.TeamLoadReport;
import com.featureflow.planning.model.ComparisonResult;
import com.featureflow.planning.model.TeamLoadDiff;
import com.featureflow.planning.model.TimelineDiff;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ComparisonServiceTest {

    @Test
    void compare_costDelta_calculatedCorrectly() {
        UUID teamId = UUID.randomUUID();
        UUID featureId = UUID.randomUUID();

        PlanningResult baseline = createResult(100.0, 2, Map.of(
            featureId, new FeatureTimeline(featureId, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 15), 0.9)
        ), Map.of(
            teamId, new TeamLoadReport(teamId, Map.of(), 80.0, List.of())
        ));

        PlanningResult simulation = createResult(120.0, 1, Map.of(
            featureId, new FeatureTimeline(featureId, LocalDate.of(2025, 1, 3), LocalDate.of(2025, 1, 17), 0.85)
        ), Map.of(
            teamId, new TeamLoadReport(teamId, Map.of(), 90.0, List.of("BACKEND"))
        ));

        ComparisonResult result = ComparisonService.compare(baseline, simulation);

        assertThat(result.costDelta()).isEqualTo(20.0);
        assertThat(result.baselineCost()).isEqualTo(100.0);
        assertThat(result.simulationCost()).isEqualTo(120.0);
    }

    @Test
    void compare_conflictDelta_calculatedCorrectly() {
        PlanningResult baseline = createResult(100.0, 3, Map.of(), Map.of());
        PlanningResult simulation = createResult(100.0, 1, Map.of(), Map.of());

        ComparisonResult result = ComparisonService.compare(baseline, simulation);

        assertThat(result.baselineConflicts()).isEqualTo(3);
        assertThat(result.simulationConflicts()).isEqualTo(1);
    }

    @Test
    void compare_timelineDiffs_includesShiftedFeatures() {
        UUID featureId = UUID.randomUUID();
        PlanningResult baseline = createResult(100.0, 0, Map.of(
            featureId, new FeatureTimeline(featureId, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 15), 0.9)
        ), Map.of());

        PlanningResult simulation = createResult(100.0, 0, Map.of(
            featureId, new FeatureTimeline(featureId, LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 19), 0.8)
        ), Map.of());

        ComparisonResult result = ComparisonService.compare(baseline, simulation);

        assertThat(result.timelineDiffs()).hasSize(1);
        TimelineDiff diff = result.timelineDiffs().get(0);
        assertThat(diff.featureId()).isEqualTo(featureId);
        assertThat(diff.dayShift()).isEqualTo(4);
        assertThat(diff.baselineStart()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(diff.simulationStart()).isEqualTo(LocalDate.of(2025, 1, 5));
    }

    @Test
    void compare_teamLoadDiffs_includesUtilizationDelta() {
        UUID teamId = UUID.randomUUID();
        PlanningResult baseline = createResult(100.0, 0, Map.of(), Map.of(
            teamId, new TeamLoadReport(teamId, Map.of(), 70.0, List.of())
        ));

        PlanningResult simulation = createResult(100.0, 0, Map.of(), Map.of(
            teamId, new TeamLoadReport(teamId, Map.of(), 85.0, List.of("BACKEND", "QA"))
        ));

        ComparisonResult result = ComparisonService.compare(baseline, simulation);

        assertThat(result.teamLoadDiffs()).hasSize(1);
        TeamLoadDiff diff = result.teamLoadDiffs().get(0);
        assertThat(diff.utilizationDelta()).isEqualTo(15.0);
        assertThat(diff.newBottleneckRoles()).containsExactly("BACKEND", "QA");
    }

    @Test
    void compare_missingSimulationFeature_noTimelineDiff() {
        UUID featureId = UUID.randomUUID();
        PlanningResult baseline = createResult(100.0, 0, Map.of(
            featureId, new FeatureTimeline(featureId, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 15), 0.9)
        ), Map.of());

        PlanningResult simulation = createResult(100.0, 0, Map.of(), Map.of());

        ComparisonResult result = ComparisonService.compare(baseline, simulation);

        assertThat(result.timelineDiffs()).isEmpty();
    }

    private PlanningResult createResult(double cost, int conflicts,
                                         Map<UUID, FeatureTimeline> timelines,
                                         Map<UUID, TeamLoadReport> teamLoads) {
        List<Conflict> conflictList = new java.util.ArrayList<>();
        for (int i = 0; i < conflicts; i++) {
            conflictList.add(new Conflict(
                Conflict.Type.CAPACITY_EXCEEDED,
                UUID.randomUUID(), UUID.randomUUID(), "Conflict " + i
            ));
        }
        return new PlanningResult(
            List.of(), conflictList, timelines, teamLoads, cost, 100, PlanningResult.PlanningAlgorithm.GREEDY
        );
    }
}
