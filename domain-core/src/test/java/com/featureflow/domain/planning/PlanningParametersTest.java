package com.featureflow.domain.planning;

import com.featureflow.domain.valueobject.AnnealingParameters;
import com.featureflow.domain.valueobject.MonteCarloParameters;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlanningParametersTest {

    @Test
    void defaults_shouldReturnValidParameters() {
        var params = PlanningParameters.defaults();
        assertThat(params.w1Ttm()).isEqualTo(1.0);
        assertThat(params.w2Underutilization()).isEqualTo(0.5);
        assertThat(params.w3DeadlinePenalty()).isEqualTo(2.0);
        assertThat(params.maxParallelFeatures()).isEqualTo(3);
    }

    @Test
    void negativeWeights_shouldThrow() {
        assertThatThrownBy(() -> new PlanningParameters(
            -1.0, 0.5, 2.0, 3,
            AnnealingParameters.defaults(), MonteCarloParameters.defaults()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void invalidMaxParallel_shouldThrow() {
        assertThatThrownBy(() -> new PlanningParameters(
            1.0, 0.5, 2.0, 0,
            AnnealingParameters.defaults(), MonteCarloParameters.defaults()
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
