package com.featureflow.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateRangeTest {

    @Test
    void days_shouldCalculateDuration() {
        var range = new DateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        assertThat(range.days()).isEqualTo(30);
    }

    @Test
    void days_shouldBeZeroForSameDay() {
        var range = new DateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1));
        assertThat(range.days()).isZero();
    }

    @Test
    void overlaps_shouldDetectIntersection() {
        var r1 = new DateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        var r2 = new DateRange(LocalDate.of(2025, 1, 15), LocalDate.of(2025, 2, 15));
        var r3 = new DateRange(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 28));

        assertThat(r1.overlaps(r2)).isTrue();
        assertThat(r2.overlaps(r1)).isTrue();
        assertThat(r1.overlaps(r3)).isFalse();
    }

    @Test
    void contains_shouldCheckDate() {
        var range = new DateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        assertThat(range.contains(LocalDate.of(2025, 1, 15))).isTrue();
        assertThat(range.contains(LocalDate.of(2025, 1, 1))).isTrue();
        assertThat(range.contains(LocalDate.of(2025, 1, 31))).isTrue();
        assertThat(range.contains(LocalDate.of(2025, 2, 1))).isFalse();
    }

    @Test
    void contains_shouldCheckDateRange() {
        var outer = new DateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        var inner = new DateRange(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 28));
        var partial = new DateRange(LocalDate.of(2025, 2, 15), LocalDate.of(2025, 4, 15));

        assertThat(outer.contains(inner)).isTrue();
        assertThat(outer.contains(partial)).isFalse();
    }

    @Test
    void withStart_shouldReturnNewRange() {
        var range = new DateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        var modified = range.withStart(LocalDate.of(2025, 1, 15));

        assertThat(modified.start()).isEqualTo(LocalDate.of(2025, 1, 15));
        assertThat(modified.end()).isEqualTo(LocalDate.of(2025, 1, 31));
    }

    @Test
    void withEnd_shouldReturnNewRange() {
        var range = new DateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        var modified = range.withEnd(LocalDate.of(2025, 2, 28));

        assertThat(modified.start()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(modified.end()).isEqualTo(LocalDate.of(2025, 2, 28));
    }

    @Test
    void endBeforeStart_shouldThrow() {
        assertThatThrownBy(() -> new DateRange(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1)))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
