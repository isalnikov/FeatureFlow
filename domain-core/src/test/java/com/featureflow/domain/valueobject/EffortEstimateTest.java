package com.featureflow.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EffortEstimateTest {

    @Test
    void totalHours_shouldSumAllRoles() {
        var estimate = new EffortEstimate(40, 24, 16, 8);
        assertThat(estimate.totalHours()).isEqualTo(88);
    }

    @Test
    void totalHours_shouldHandleZero() {
        var estimate = new EffortEstimate();
        assertThat(estimate.totalHours()).isZero();
    }

    @Test
    void forRole_shouldReturnCorrectValue() {
        var estimate = new EffortEstimate(40, 24, 16, 8);
        assertThat(estimate.forRole(Role.BACKEND)).isEqualTo(40);
        assertThat(estimate.forRole(Role.FRONTEND)).isEqualTo(24);
        assertThat(estimate.forRole(Role.QA)).isEqualTo(16);
        assertThat(estimate.forRole(Role.DEVOPS)).isEqualTo(8);
    }

    @Test
    void negativeHours_shouldThrow() {
        assertThatThrownBy(() -> new EffortEstimate(-1, 0, 0, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Effort cannot be negative");
    }

    @Test
    void withScaledEffort_shouldMultiplyAllRoles() {
        var estimate = new EffortEstimate(40, 24, 16, 8);
        var scaled = estimate.withScaledEffort(1.5);

        assertThat(scaled.backendHours()).isEqualTo(60);
        assertThat(scaled.frontendHours()).isEqualTo(36);
        assertThat(scaled.qaHours()).isEqualTo(24);
        assertThat(scaled.devopsHours()).isEqualTo(12);
    }

    @Test
    void add_shouldCombineEstimates() {
        var e1 = new EffortEstimate(40, 24, 16, 8);
        var e2 = new EffortEstimate(20, 12, 8, 4);
        var sum = e1.add(e2);

        assertThat(sum.backendHours()).isEqualTo(60);
        assertThat(sum.frontendHours()).isEqualTo(36);
        assertThat(sum.qaHours()).isEqualTo(24);
        assertThat(sum.devopsHours()).isEqualTo(12);
    }
}
