package com.featureflow.planning.montecarlo;

import com.featureflow.domain.entity.FeatureRequest;
import com.featureflow.domain.planning.*;
import com.featureflow.domain.valueobject.EffortEstimate;
import com.featureflow.domain.valueobject.MonteCarloParameters;
import com.featureflow.domain.valueobject.ThreePointEstimate;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class MonteCarloSimulatorTest {

    @Test
    void simulate_probabilitiesInRange() {
        UUID f1 = uuid("f1");
        FeatureRequest feature = new FeatureRequest(f1, "f1", "", 50.0);
        feature.setDeadline(LocalDate.of(2025, 2, 1));
        feature.setEffortEstimate(new EffortEstimate(40, 0, 0, 0));
        feature.setStochasticEstimate(new ThreePointEstimate(30, 40, 60));

        MonteCarloParameters params = new MonteCarloParameters(100, 0.95);

        MonteCarloSimulator simulator = new MonteCarloSimulator();
        Map<UUID, FeatureDeadlineProbability> results = simulator.simulate(
            List.of(feature),
            Map.of(f1, LocalDate.of(2025, 1, 15)),
            params,
            new Random(42)
        );

        assertThat(results).containsKey(f1);
        FeatureDeadlineProbability prob = results.get(f1);
        assertThat(prob.probability()).isBetween(0.0, 1.0);
    }

    @Test
    void simulate_noDeadline_returnsZeroProbability() {
        UUID f1 = uuid("f1");
        FeatureRequest feature = new FeatureRequest(f1, "f1", "", 50.0);
        feature.setEffortEstimate(new EffortEstimate(40, 0, 0, 0));

        MonteCarloParameters params = new MonteCarloParameters(50, 0.95);

        MonteCarloSimulator simulator = new MonteCarloSimulator();
        Map<UUID, FeatureDeadlineProbability> results = simulator.simulate(
            List.of(feature),
            Map.of(f1, LocalDate.of(2025, 1, 15)),
            params,
            new Random(42)
        );

        FeatureDeadlineProbability prob = results.get(f1);
        assertThat(prob.probability()).isEqualTo(0.0);
        assertThat(prob.deadline()).isNull();
    }

    @Test
    void simulate_earlyDeadline_lowProbability() {
        UUID f1 = uuid("f1");
        FeatureRequest feature = new FeatureRequest(f1, "f1", "", 50.0);
        feature.setDeadline(LocalDate.of(2025, 1, 5));
        feature.setEffortEstimate(new EffortEstimate(40, 0, 0, 0));
        feature.setStochasticEstimate(new ThreePointEstimate(30, 40, 60));

        MonteCarloParameters params = new MonteCarloParameters(200, 0.95);

        MonteCarloSimulator simulator = new MonteCarloSimulator();
        Map<UUID, FeatureDeadlineProbability> results = simulator.simulate(
            List.of(feature),
            Map.of(f1, LocalDate.of(2025, 1, 15)),
            params,
            new Random(42)
        );

        FeatureDeadlineProbability prob = results.get(f1);
        assertThat(prob.probability()).isLessThan(0.5);
    }

    private UUID uuid(String suffix) {
        return UUID.nameUUIDFromBytes(suffix.getBytes());
    }
}
