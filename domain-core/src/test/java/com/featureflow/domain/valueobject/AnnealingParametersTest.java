package com.featureflow.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnnealingParametersTest {

    @Test
    void defaults_shouldReturnValidParameters() {
        var params = AnnealingParameters.defaults();
        assertThat(params.initialTemperature()).isEqualTo(1000.0);
        assertThat(params.coolingRate()).isEqualTo(0.95);
        assertThat(params.minTemperature()).isEqualTo(0.1);
        assertThat(params.maxIterations()).isEqualTo(10000);
    }

    @Test
    void create_shouldSetFields() {
        var params = new AnnealingParameters(500.0, 0.9, 0.01, 5000);
        assertThat(params.initialTemperature()).isEqualTo(500.0);
        assertThat(params.coolingRate()).isEqualTo(0.9);
        assertThat(params.minTemperature()).isEqualTo(0.01);
        assertThat(params.maxIterations()).isEqualTo(5000);
    }
}
