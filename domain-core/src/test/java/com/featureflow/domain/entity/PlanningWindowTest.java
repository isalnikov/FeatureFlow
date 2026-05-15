package com.featureflow.domain.entity;

import com.featureflow.domain.valueobject.DateRange;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlanningWindowTest {

    @Test
    void create_shouldInitializeDefaults() {
        var window = new PlanningWindow(
            UUID.randomUUID(),
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 3, 31)
        );
        assertThat(window.getSprints()).isEmpty();
    }

    @Test
    void addSprint_shouldAddToList() {
        var window = new PlanningWindow(
            UUID.randomUUID(),
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 3, 31)
        );
        var sprint = new Sprint(UUID.randomUUID(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 14));
        window.addSprint(sprint);

        assertThat(window.getSprints()).hasSize(1);
    }

    @Test
    void dateRange_shouldReturnCorrectRange() {
        var window = new PlanningWindow(
            UUID.randomUUID(),
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 3, 31)
        );
        var range = window.dateRange();
        assertThat(range.start()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(range.end()).isEqualTo(LocalDate.of(2025, 3, 31));
    }

    @Test
    void sprintIndexForDate_shouldFindCorrectIndex() {
        var window = new PlanningWindow(
            UUID.randomUUID(),
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 3, 31)
        );
        window.addSprint(new Sprint(UUID.randomUUID(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 14)));
        window.addSprint(new Sprint(UUID.randomUUID(), LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 28)));

        assertThat(window.sprintIndexForDate(LocalDate.of(2025, 1, 7))).isEqualTo(0);
        assertThat(window.sprintIndexForDate(LocalDate.of(2025, 1, 20))).isEqualTo(1);
        assertThat(window.sprintIndexForDate(LocalDate.of(2025, 3, 1))).isEqualTo(-1);
    }

    @Test
    void getSprint_shouldReturnCorrectSprint() {
        var window = new PlanningWindow(
            UUID.randomUUID(),
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 3, 31)
        );
        var sprint = new Sprint(UUID.randomUUID(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 14));
        window.addSprint(sprint);

        assertThat(window.getSprint(0)).isEqualTo(sprint);
        assertThat(window.getSprint(1)).isNull();
    }

    @Test
    void endDateBeforeStart_shouldThrow() {
        assertThatThrownBy(() -> new PlanningWindow(
            UUID.randomUUID(),
            LocalDate.of(2025, 3, 31),
            LocalDate.of(2025, 1, 1)
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equality_shouldBeBasedOnId() {
        UUID id = UUID.randomUUID();
        var w1 = new PlanningWindow(id, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        var w2 = new PlanningWindow(id, LocalDate.of(2025, 4, 1), LocalDate.of(2025, 6, 30));

        assertThat(w1).isEqualTo(w2);
    }
}
