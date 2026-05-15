package com.featureflow.domain.planning;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FeatureTimelineTest {

    @Test
    void create_shouldSetFields() {
        var timeline = new FeatureTimeline(
            UUID.randomUUID(),
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 14),
            0.85
        );
        assertThat(timeline.featureId()).isNotNull();
        assertThat(timeline.startDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(timeline.endDate()).isEqualTo(LocalDate.of(2025, 1, 14));
        assertThat(timeline.probabilityOfMeetingDeadline()).isEqualTo(0.85);
    }
}
