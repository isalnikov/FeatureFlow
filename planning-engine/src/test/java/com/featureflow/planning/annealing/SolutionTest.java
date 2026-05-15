package com.featureflow.planning.annealing;

import com.featureflow.domain.entity.Assignment;
import com.featureflow.domain.entity.FeatureRequest;
import com.featureflow.domain.entity.Product;
import com.featureflow.domain.entity.Team;
import com.featureflow.domain.planning.FeatureTimeline;
import com.featureflow.domain.planning.PlanningParameters;
import com.featureflow.domain.planning.PlanningRequest;
import com.featureflow.domain.planning.PlanningResult;
import com.featureflow.domain.valueobject.EffortEstimate;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class SolutionTest {

    @Test
    void fromPlanningResult_createsSolutionWithCorrectMapping() {
        UUID f1 = uuid("f1");
        UUID f2 = uuid("f2");
        UUID t1 = uuid("t1");
        UUID s1 = uuid("s1");
        UUID s2 = uuid("s2");

        List<Assignment> assignments = List.of(
            new Assignment(uuid("a1"), f1, t1, s1),
            new Assignment(uuid("a2"), f2, t1, s2)
        );

        Map<UUID, FeatureTimeline> timelines = Map.of(
            f1, new FeatureTimeline(f1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 14), 1.0),
            f2, new FeatureTimeline(f2, LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 28), 1.0)
        );

        PlanningResult result = new PlanningResult(
            assignments, List.of(), timelines, Map.of(), 100.0, 50,
            PlanningResult.PlanningAlgorithm.GREEDY
        );

        Solution solution = Solution.from(result);

        assertThat(solution.featureTeamMapping()).containsEntry(f1, t1);
        assertThat(solution.featureSprintIndex()).containsEntry(f1, 0);
        assertThat(solution.featureSprintIndex()).containsEntry(f2, 1);
    }

    @Test
    void computeCost_weightedTtm_returnsWeightedCompletionDates() {
        UUID f1 = uuid("f1");
        UUID f2 = uuid("f2");

        FeatureRequest feature1 = feature(f1, 100.0);
        FeatureRequest feature2 = feature(f2, 50.0);

        Map<UUID, FeatureTimeline> timelines = Map.of(
            f1, new FeatureTimeline(f1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 10), 1.0),
            f2, new FeatureTimeline(f2, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 20), 1.0)
        );

        PlanningParameters params = PlanningParameters.defaults();

        double cost = Solution.computeWeightedTtm(
            List.of(feature1, feature2),
            timelines,
            params.w1Ttm()
        );

        double expected = 100.0 * 9 + 50.0 * 19;
        assertThat(cost).isEqualTo(expected);
    }

    private UUID uuid(String suffix) {
        return UUID.nameUUIDFromBytes(suffix.getBytes());
    }

    private FeatureRequest feature(UUID id, double value) {
        FeatureRequest f = new FeatureRequest(id, "feature-" + id, "", value);
        f.setEffortEstimate(new EffortEstimate(10, 0, 0, 0));
        return f;
    }
}
