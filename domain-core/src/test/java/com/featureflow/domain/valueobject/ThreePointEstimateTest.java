package com.featureflow.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThreePointEstimateTest {

    @Test
    void expected_shouldUsePERTFormula() {
        var estimate = new ThreePointEstimate(30, 48, 72);
        assertThat(estimate.expected()).isEqualTo(49.0);
    }

    @Test
    void standardDeviation_shouldBeCorrect() {
        var estimate = new ThreePointEstimate(30, 48, 72);
        assertThat(estimate.standardDeviation()).isEqualTo(7.0);
    }

    @Test
    void variance_shouldBeSquaredStandardDeviation() {
        var estimate = new ThreePointEstimate(30, 48, 72);
        assertThat(estimate.variance()).isEqualTo(49.0);
    }

    @Test
    void negativeValues_shouldThrow() {
        assertThatThrownBy(() -> new ThreePointEstimate(-1, 10, 20))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Estimates cannot be negative");
    }

    @Test
    void invalidRange_shouldThrow() {
        assertThatThrownBy(() -> new ThreePointEstimate(50, 40, 60))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid estimate range");
    }

    @Test
    void equalValues_shouldBeValid() {
        var estimate = new ThreePointEstimate(40, 40, 40);
        assertThat(estimate.expected()).isEqualTo(40.0);
        assertThat(estimate.standardDeviation()).isZero();
    }
}
