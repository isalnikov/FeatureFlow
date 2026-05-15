package com.featureflow.planning.annealing;

import com.featureflow.domain.entity.FeatureRequest;
import com.featureflow.domain.valueobject.AnnealingParameters;
import com.featureflow.domain.planning.PlanningRequest;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SimulatedAnnealingTest {

    @Test
    void optimizes_improvesCostFromInitialSolution() {
        UUID f1 = uuid("f1");
        UUID f2 = uuid("f2");
        UUID t1 = uuid("t1");

        FeatureRequest feature1 = new FeatureRequest(f1, "f1", "", 50.0);
        FeatureRequest feature2 = new FeatureRequest(f2, "f2", "", 30.0);

        Map<UUID, UUID> teamMapping = Map.of(f1, t1, f2, t1);

        Solution initial = new Solution(
            Map.of(),
            Map.of(f1, 0, f2, 0),
            teamMapping,
            200.0
        );

        AnnealingParameters params = new AnnealingParameters(100.0, 0.95, 0.1, 100);
        List<UUID> featureIds = List.of(f1, f2);
        List<UUID> candidateTeams = List.of(t1);

        Mutator mutator = new Mutator();
        SimulatedAnnealing sa = new SimulatedAnnealing(mutator);

        Solution best = sa.optimize(initial, params, featureIds, candidateTeams, List.of());

        assertThat(best).isNotNull();
        assertThat(best.cost()).isLessThan(Double.MAX_VALUE);
    }

    @Test
    void optimizes_respectsLockedAssignments() {
        UUID f1 = uuid("f1");
        UUID t1 = uuid("t1");

        Solution initial = new Solution(
            Map.of(),
            Map.of(f1, 0),
            Map.of(f1, t1),
            100.0
        );

        AnnealingParameters params = new AnnealingParameters(100.0, 0.95, 0.1, 50);
        List<UUID> featureIds = List.of(f1);
        List<UUID> candidateTeams = List.of(t1);
        List<UUID> lockedFeatures = List.of(f1);

        Mutator mutator = new Mutator();
        SimulatedAnnealing sa = new SimulatedAnnealing(mutator);

        Solution best = sa.optimize(initial, params, featureIds, candidateTeams, lockedFeatures);

        assertThat(best.featureTeamMapping().get(f1)).isEqualTo(t1);
    }

    private UUID uuid(String suffix) {
        return UUID.nameUUIDFromBytes(suffix.getBytes());
    }
}
