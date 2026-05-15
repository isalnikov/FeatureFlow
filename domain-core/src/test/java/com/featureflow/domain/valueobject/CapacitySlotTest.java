package com.featureflow.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CapacitySlotTest {

    @Test
    void canFit_shouldReturnTrueWhenEnoughCapacity() {
        var slot = new CapacitySlot(UUID.randomUUID(), 0, new EffortEstimate(40, 40, 20, 10));
        var effort = new EffortEstimate(20, 20, 10, 5);

        assertThat(slot.canFit(effort)).isTrue();
    }

    @Test
    void canFit_shouldReturnFalseWhenNotEnoughCapacity() {
        var slot = new CapacitySlot(UUID.randomUUID(), 0, new EffortEstimate(10, 10, 5, 5));
        var effort = new EffortEstimate(20, 20, 10, 5);

        assertThat(slot.canFit(effort)).isFalse();
    }

    @Test
    void deduct_shouldReduceAvailableEffort() {
        var slot = new CapacitySlot(UUID.randomUUID(), 0, new EffortEstimate(40, 40, 20, 10));
        var effort = new EffortEstimate(20, 10, 5, 3);

        var deducted = slot.deduct(effort);

        assertThat(deducted.availableEffort().backendHours()).isEqualTo(20);
        assertThat(deducted.availableEffort().frontendHours()).isEqualTo(30);
        assertThat(deducted.availableEffort().qaHours()).isEqualTo(15);
        assertThat(deducted.availableEffort().devopsHours()).isEqualTo(7);
    }

    @Test
    void deduct_shouldNotGoBelowZero() {
        var slot = new CapacitySlot(UUID.randomUUID(), 0, new EffortEstimate(10, 10, 5, 5));
        var effort = new EffortEstimate(20, 15, 10, 10);

        var deducted = slot.deduct(effort);

        assertThat(deducted.availableEffort().backendHours()).isZero();
        assertThat(deducted.availableEffort().frontendHours()).isZero();
        assertThat(deducted.availableEffort().qaHours()).isZero();
        assertThat(deducted.availableEffort().devopsHours()).isZero();
    }
}
