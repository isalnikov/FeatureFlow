package com.featureflow.domain.planning;

import com.featureflow.domain.entity.Assignment;
import com.featureflow.domain.valueobject.AnnealingParameters;
import com.featureflow.domain.valueobject.EffortEstimate;
import com.featureflow.domain.valueobject.MonteCarloParameters;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlanningResultTest {

    @Test
    void planningAlgorithm_enumValues() {
        assertThat(PlanningResult.PlanningAlgorithm.values())
            .containsExactly(
                PlanningResult.PlanningAlgorithm.GREEDY,
                PlanningResult.PlanningAlgorithm.SIMULATED_ANNEALING,
                PlanningResult.PlanningAlgorithm.MONTE_CARLO
            );
    }

    @Test
    void create_shouldSetFields() {
        var result = new PlanningResult(
            List.of(),
            List.of(),
            Map.of(),
            Map.of(),
            0.0,
            100L,
            PlanningResult.PlanningAlgorithm.GREEDY
        );

        assertThat(result.assignments()).isEmpty();
        assertThat(result.conflicts()).isEmpty();
        assertThat(result.featureTimelines()).isEmpty();
        assertThat(result.teamLoadReports()).isEmpty();
        assertThat(result.totalCost()).isZero();
        assertThat(result.computationTimeMs()).isEqualTo(100L);
        assertThat(result.algorithm()).isEqualTo(PlanningResult.PlanningAlgorithm.GREEDY);
    }
}
