package com.featureflow.domain.entity;

import com.featureflow.domain.valueobject.Capacity;
import com.featureflow.domain.valueobject.DateRange;
import com.featureflow.domain.valueobject.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SprintTest {

    @Test
    void dateRange_shouldReturnCorrectRange() {
        var sprint = new Sprint(
            UUID.randomUUID(),
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 14)
        );
        var range = sprint.dateRange();
        assertThat(range.start()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(range.end()).isEqualTo(LocalDate.of(2025, 1, 14));
    }

    @Test
    void containsDate_shouldCheckCorrectly() {
        var sprint = new Sprint(
            UUID.randomUUID(),
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 14)
        );
        assertThat(sprint.containsDate(LocalDate.of(2025, 1, 7))).isTrue();
        assertThat(sprint.containsDate(LocalDate.of(2025, 1, 15))).isFalse();
    }

    @Test
    void durationDays_shouldCalculateCorrectly() {
        var sprint = new Sprint(
            UUID.randomUUID(),
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 14)
        );
        assertThat(sprint.durationDays()).isEqualTo(13);
    }

    @Test
    void capacityOverrides_shouldBeAccessible() {
        UUID teamId = UUID.randomUUID();
        var override = new Capacity(Map.of(Role.BACKEND, 80.0), 0.5, 0.1, 0.05);
        var sprint = new Sprint(
            UUID.randomUUID(),
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 14),
            Map.of(teamId, override)
        );
        assertThat(sprint.capacityOverrides()).containsKey(teamId);
    }

    @Test
    void endDateBeforeStart_shouldThrow() {
        assertThatThrownBy(() -> new Sprint(
            UUID.randomUUID(),
            LocalDate.of(2025, 1, 14),
            LocalDate.of(2025, 1, 1)
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
