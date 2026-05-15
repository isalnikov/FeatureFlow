package com.featureflow.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CapacityTest {

    @Test
    void effectiveHours_shouldApplyFocusFactorAndReserves() {
        var capacity = new Capacity(
            Map.of(Role.BACKEND, 160.0, Role.FRONTEND, 120.0, Role.QA, 80.0, Role.DEVOPS, 40.0),
            0.7,
            0.20,
            0.10
        );
        assertThat(capacity.effectiveHours(Role.BACKEND)).isEqualTo(78.4);
    }

    @Test
    void effectiveHours_shouldReturnZeroForMissingRole() {
        var capacity = new Capacity(Map.of(Role.BACKEND, 160.0), 0.7, 0.20, 0.10);
        assertThat(capacity.effectiveHours(Role.QA)).isZero();
    }

    @Test
    void totalEffectiveHours_shouldSumAllRoles() {
        var capacity = new Capacity(
            Map.of(Role.BACKEND, 100.0, Role.FRONTEND, 100.0),
            0.5,
            0.0,
            0.0
        );
        assertThat(capacity.totalEffectiveHours()).isEqualTo(100.0);
    }

    @Test
    void hasCapacityForRole_shouldCheckRequirement() {
        var capacity = new Capacity(
            Map.of(Role.BACKEND, 160.0),
            0.7,
            0.20,
            0.10
        );
        assertThat(capacity.hasCapacityFor(Role.BACKEND, 50.0)).isTrue();
        assertThat(capacity.hasCapacityFor(Role.BACKEND, 100.0)).isFalse();
    }

    @Test
    void withFocusFactor_shouldReturnNewCapacity() {
        var capacity = new Capacity(
            Map.of(Role.BACKEND, 160.0),
            0.7,
            0.20,
            0.10
        );
        var modified = capacity.withFocusFactor(0.5);

        assertThat(modified.focusFactor()).isEqualTo(0.5);
        assertThat(capacity.focusFactor()).isEqualTo(0.7);
    }

    @Test
    void emptyCapacity_shouldHaveZeroHours() {
        var capacity = Capacity.empty();
        assertThat(capacity.hoursPerRole()).isEmpty();
        assertThat(capacity.focusFactor()).isEqualTo(1.0);
    }

    @Test
    void invalidFocusFactor_shouldThrow() {
        assertThatThrownBy(() -> new Capacity(Map.of(), 0, 0, 0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Capacity(Map.of(), 1.5, 0, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void invalidReserves_shouldThrow() {
        assertThatThrownBy(() -> new Capacity(Map.of(), 0.7, 1.5, 0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Capacity(Map.of(), 0.7, 0.6, 0.5))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
