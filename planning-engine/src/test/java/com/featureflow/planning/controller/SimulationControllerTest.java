package com.featureflow.planning.controller;

import com.featureflow.domain.entity.FeatureRequest;
import com.featureflow.domain.entity.Team;
import com.featureflow.domain.planning.PlanningRequest;
import com.featureflow.domain.planning.PlanningResult;
import com.featureflow.domain.planning.PlanningParameters;
import com.featureflow.domain.valueobject.AnnealingParameters;
import com.featureflow.domain.valueobject.MonteCarloParameters;
import com.featureflow.planning.model.SimulationChanges;
import com.featureflow.planning.model.SimulationRequest;
import com.featureflow.planning.service.PlanningJobService;
import com.featureflow.planning.service.PlanningOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulationControllerTest {

    @Mock
    private PlanningOrchestrator orchestrator;
    @Mock
    private PlanningJobService jobService;

    private SimulationController controller;

    @BeforeEach
    void setUp() {
        controller = new SimulationController(orchestrator, jobService);
    }

    @Test
    void runSimulation_appliesPriorityChanges() {
        UUID featureId = UUID.randomUUID();
        FeatureRequest feature = new FeatureRequest(featureId, "Test", "Desc", 50.0);
        feature.setEffortEstimate(new com.featureflow.domain.valueobject.EffortEstimate(10, 5, 3, 1));

        Team team = new Team(UUID.randomUUID(), "Team A");

        PlanningRequest baseRequest = new PlanningRequest(
            List.of(feature), List.of(team), List.of(), null,
            new PlanningParameters(1.0, 0.5, 2.0, 3,
                new AnnealingParameters(1000, 0.95, 0.1, 10000),
                new MonteCarloParameters(500, 0.95)),
            Set.of()
        );

        SimulationChanges changes = new SimulationChanges(
            Map.of(featureId, 80.0), null, null, null
        );
        SimulationRequest request = new SimulationRequest(baseRequest, changes);

        UUID jobId = UUID.randomUUID();
        when(jobService.submitJob(any(PlanningRequest.class))).thenReturn(jobId);

        var response = controller.runSimulation(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsEntry("jobId", jobId.toString());
    }

    @Test
    void runSimulation_appliesRemovedFeatures() {
        UUID featureId1 = UUID.randomUUID();
        UUID featureId2 = UUID.randomUUID();
        FeatureRequest f1 = new FeatureRequest(featureId1, "F1", "Desc", 50.0);
        FeatureRequest f2 = new FeatureRequest(featureId2, "F2", "Desc", 60.0);
        Team team = new Team(UUID.randomUUID(), "Team A");

        PlanningRequest baseRequest = new PlanningRequest(
            List.of(f1, f2), List.of(team), List.of(), null,
            new PlanningParameters(1.0, 0.5, 2.0, 3,
                new AnnealingParameters(1000, 0.95, 0.1, 10000),
                new MonteCarloParameters(500, 0.95)),
            Set.of()
        );

        SimulationChanges changes = new SimulationChanges(
            null, null, null, List.of(featureId1)
        );

        UUID jobId = UUID.randomUUID();
        when(jobService.submitJob(any(PlanningRequest.class))).thenReturn(jobId);

        var response = controller.runSimulation(new SimulationRequest(baseRequest, changes));

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }
}
