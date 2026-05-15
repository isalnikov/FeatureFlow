package com.featureflow.planning.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PlanningRunRequestTest {

    @Test
    void createsRequestWithAllFields() {
        UUID featureId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID windowId = UUID.randomUUID();

        var params = new PlanningRunRequest.PlanningParametersDto(
            1.0, 0.5, 2.0, 3,
            new PlanningRunRequest.AnnealingParamsDto(1000.0, 0.95, 0.1, 10000),
            new PlanningRunRequest.MonteCarloParamsDto(1000, 0.95)
        );

        PlanningRunRequest request = new PlanningRunRequest(
            List.of(featureId),
            List.of(teamId),
            windowId,
            params,
            Set.of()
        );

        assertThat(request.featureIds()).containsExactly(featureId);
        assertThat(request.teamIds()).containsExactly(teamId);
        assertThat(request.planningWindowId()).isEqualTo(windowId);
        assertThat(request.parameters()).isEqualTo(params);
        assertThat(request.lockedAssignmentIds()).isEmpty();
    }
}
