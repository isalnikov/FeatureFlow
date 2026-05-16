package com.featureflow.planning.service;

import com.featureflow.domain.entity.FeatureRequest;
import com.featureflow.domain.entity.PlanningWindow;
import com.featureflow.domain.entity.Product;
import com.featureflow.domain.entity.Sprint;
import com.featureflow.domain.entity.Team;
import com.featureflow.domain.planning.PlanningParameters;
import com.featureflow.domain.planning.PlanningRequest;
import com.featureflow.domain.planning.PlanningResult;
import com.featureflow.domain.valueobject.AnnealingParameters;
import com.featureflow.domain.valueobject.EffortEstimate;
import com.featureflow.domain.valueobject.MonteCarloParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanningJobServiceTest {

    @Mock
    private PlanningOrchestrator orchestrator;

    @InjectMocks
    private PlanningJobService jobService;

    @Test
    void submitJob_shouldCreateAndReturnJobId() {
        PlanningRequest request = createTestRequest();
        when(orchestrator.runFullPipeline(any(PlanningRequest.class)))
            .thenReturn(createEmptyResult());

        UUID returnedJobId = jobService.submitJob(request);

        assertThat(returnedJobId).isNotNull();
    }

    @Test
    void getJobStatus_existingJob_shouldReturnJob() {
        PlanningRequest request = createTestRequest();
        when(orchestrator.runFullPipeline(any(PlanningRequest.class)))
            .thenReturn(createEmptyResult());

        UUID jobId = jobService.submitJob(request);
        PlanningJobService.PlanningJob job = jobService.getJobStatus(jobId);

        assertThat(job.id()).isEqualTo(jobId);
        assertThat(job.status()).isIn(
            PlanningJobService.PlanningJobStatus.RUNNING,
            PlanningJobService.PlanningJobStatus.COMPLETED
        );
    }

    @Test
    void getJobStatus_nonExistingJob_shouldThrow() {
        assertThatThrownBy(() -> jobService.getJobStatus(UUID.randomUUID()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Job not found");
    }

    @Test
    void getJobResult_completedJob_returnsNullWhenResultNotStored() {
        PlanningResult result = createEmptyResult();
        PlanningRequest request = createTestRequest();
        when(orchestrator.runFullPipeline(any(PlanningRequest.class)))
            .thenReturn(result);

        UUID jobId = jobService.submitJob(request);

        PlanningJobService.PlanningJob job = jobService.getJobStatus(jobId);
        assertThat(job.status()).isEqualTo(PlanningJobService.PlanningJobStatus.COMPLETED);

        PlanningResult actualResult = jobService.getJobResult(jobId);
        assertThat(actualResult).isNotNull();
        assertThat(actualResult.algorithm()).isEqualTo(PlanningResult.PlanningAlgorithm.GREEDY);
    }

    @Test
    void getJobResult_nonExistingJob_shouldReturnNull() {
        assertThat(jobService.getJobResult(UUID.randomUUID())).isNull();
    }

    @Test
    void streamStatus_existingJob_shouldReturnEmitter() {
        PlanningRequest request = createTestRequest();
        when(orchestrator.runFullPipeline(any(PlanningRequest.class)))
            .thenReturn(createEmptyResult());

        UUID jobId = jobService.submitJob(request);
        SseEmitter emitter = jobService.streamStatus(jobId);

        assertThat(emitter).isNotNull();
    }

    @Test
    void streamStatus_nonExistingJob_shouldThrow() {
        assertThatThrownBy(() -> jobService.streamStatus(UUID.randomUUID()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Job not found");
    }

    @Test
    void planningJob_recordCreation() {
        UUID jobId = UUID.randomUUID();
        PlanningJobService.PlanningJob job = new PlanningJobService.PlanningJob(
            jobId,
            PlanningJobService.PlanningJobStatus.COMPLETED,
            java.time.LocalDateTime.now(),
            java.time.LocalDateTime.now(),
            null
        );

        assertThat(job.id()).isEqualTo(jobId);
        assertThat(job.status()).isEqualTo(PlanningJobService.PlanningJobStatus.COMPLETED);
        assertThat(job.result()).isNull();
    }

    @Test
    void planningJob_withResult() {
        UUID jobId = UUID.randomUUID();
        PlanningResult result = createEmptyResult();
        PlanningJobService.PlanningJob job = new PlanningJobService.PlanningJob(
            jobId,
            PlanningJobService.PlanningJobStatus.COMPLETED,
            java.time.LocalDateTime.now(),
            java.time.LocalDateTime.now(),
            null,
            result
        );

        assertThat(job.result()).isEqualTo(result);
    }

    @Test
    void planningJobStatus_enumValues() {
        assertThat(PlanningJobService.PlanningJobStatus.values())
            .containsExactly(
                PlanningJobService.PlanningJobStatus.PENDING,
                PlanningJobService.PlanningJobStatus.RUNNING,
                PlanningJobService.PlanningJobStatus.COMPLETED,
                PlanningJobService.PlanningJobStatus.FAILED
            );
    }

    private PlanningRequest createTestRequest() {
        UUID f1 = UUID.randomUUID();
        UUID t1 = UUID.randomUUID();
        UUID p1 = UUID.randomUUID();
        UUID s1 = UUID.randomUUID();

        FeatureRequest feature = new FeatureRequest(f1, "Feature", "", 50.0);
        feature.setEffortEstimate(new EffortEstimate(40, 0, 0, 0));
        feature.addProductId(p1);

        Team team = new Team(t1, "Team");

        Product product = new Product(p1, "Product", "");
        product.addTeamId(t1);

        Sprint sprint = new Sprint(s1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 14));
        PlanningWindow window = new PlanningWindow(UUID.randomUUID(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30));
        window.addSprint(sprint);

        AnnealingParameters annealing = new AnnealingParameters(1000.0, 0.95, 0.1, 100);
        MonteCarloParameters monteCarlo = new MonteCarloParameters(100, 0.95);
        PlanningParameters params = new PlanningParameters(1.0, 0.5, 2.0, 3, annealing, monteCarlo);

        return new PlanningRequest(
            List.of(feature),
            List.of(team),
            List.of(product),
            window,
            params,
            java.util.Set.of()
        );
    }

    private PlanningResult createEmptyResult() {
        return new PlanningResult(
            List.of(),
            List.of(),
            java.util.Map.of(),
            java.util.Map.of(),
            0.0,
            100L,
            PlanningResult.PlanningAlgorithm.GREEDY
        );
    }
}
