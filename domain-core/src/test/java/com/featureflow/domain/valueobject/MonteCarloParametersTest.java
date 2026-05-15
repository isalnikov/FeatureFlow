package com.featureflow.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MonteCarloParametersTest {

    @Test
    void defaults_shouldReturnValidParameters() {
        var params = MonteCarloParameters.defaults();
        assertThat(params.iterations()).isEqualTo(1000);
        assertThat(params.confidenceLevel()).isEqualTo(0.95);
    }

    @Test
    void create_shouldSetFields() {
        var params = new MonteCarloParameters(500, 0.90);
        assertThat(params.iterations()).isEqualTo(500);
        assertThat(params.confidenceLevel()).isEqualTo(0.90);
    }
}
