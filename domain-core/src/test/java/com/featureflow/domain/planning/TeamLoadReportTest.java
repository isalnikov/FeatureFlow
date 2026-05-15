package com.featureflow.domain.planning;

import com.featureflow.domain.valueobject.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TeamLoadReportTest {

    @Test
    void create_shouldSetFields() {
        var report = new TeamLoadReport(
            UUID.randomUUID(),
            Map.of(LocalDate.of(2025, 1, 1), Map.of(Role.BACKEND, 80.0)),
            75.0,
            List.of("QA")
        );
        assertThat(report.teamId()).isNotNull();
        assertThat(report.loadByDateAndRole()).hasSize(1);
        assertThat(report.utilizationPercent()).isEqualTo(75.0);
        assertThat(report.bottleneckRoles()).contains("QA");
    }
}
