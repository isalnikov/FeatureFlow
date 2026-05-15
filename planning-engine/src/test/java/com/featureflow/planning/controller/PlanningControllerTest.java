package com.featureflow.planning.controller;

import com.featureflow.domain.entity.*;
import com.featureflow.domain.planning.*;
import com.featureflow.domain.valueobject.Capacity;
import com.featureflow.domain.valueobject.EffortEstimate;
import com.featureflow.domain.valueobject.Role;
import com.featureflow.planning.model.PlanningRunRequest;
import com.featureflow.planning.service.PlanningJobService;
import com.featureflow.planning.service.PlanningOrchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlanningController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlanningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlanningOrchestrator orchestrator;

    @MockitoBean
    private PlanningJobService jobService;

    @Test
    void executePlanning_returnsResult() throws Exception {
        PlanningRequest request = createTestRequest();
        PlanningResult result = createTestResult();

        when(orchestrator.runFullPipeline(any())).thenReturn(result);

        mockMvc.perform(post("/api/v1/planning/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.algorithm").value("SIMULATED_ANNEALING"))
            .andExpect(jsonPath("$.totalCost").value(80.0));
    }

    @Test
    void runPlanning_submitsJob_returnsAccepted() throws Exception {
        PlanningRequest request = createTestRequest();
        UUID jobId = UUID.randomUUID();

        when(jobService.submitJob(any())).thenReturn(jobId);

        mockMvc.perform(post("/api/v1/planning/run")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(header().string("X-Job-Id", jobId.toString()))
            .andExpect(jsonPath("$.jobId").value(jobId.toString()))
            .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(jobService).submitJob(any(PlanningRequest.class));
    }

    @Test
    void getJobStatus_existingJob_returnsJob() throws Exception {
        UUID jobId = UUID.randomUUID();
        PlanningJobService.PlanningJob job = new PlanningJobService.PlanningJob(
            jobId,
            PlanningJobService.PlanningJobStatus.COMPLETED,
            java.time.LocalDateTime.now(),
            java.time.LocalDateTime.now(),
            null
        );

        when(jobService.getJobStatus(jobId)).thenReturn(job);

        mockMvc.perform(get("/api/v1/planning/jobs/{jobId}/status", jobId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(jobId.toString()))
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void getJobStatus_nonExistingJob_returns404() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(jobService.getJobStatus(jobId)).thenThrow(new IllegalArgumentException("Job not found"));

        mockMvc.perform(get("/api/v1/planning/jobs/{jobId}/status", jobId))
            .andExpect(status().isNotFound());
    }

    @Test
    void getResult_completedJob_returnsResult() throws Exception {
        UUID jobId = UUID.randomUUID();
        PlanningResult result = createTestResult();

        when(jobService.getJobResult(jobId)).thenReturn(result);

        mockMvc.perform(get("/api/v1/planning/jobs/{jobId}/result", jobId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCost").value(80.0));
    }

    @Test
    void getResult_noResult_returns404() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(jobService.getJobResult(jobId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/planning/jobs/{jobId}/result", jobId))
            .andExpect(status().isNotFound());
    }

    @Test
    void validate_withValidRequest_returnsOk() throws Exception {
        PlanningRunRequest request = new PlanningRunRequest(
            List.of(UUID.randomUUID()),
            List.of(UUID.randomUUID()),
            UUID.randomUUID(),
            null,
            Set.of()
        );

        when(orchestrator.validate(any())).thenReturn(List.of());

        mockMvc.perform(post("/api/v1/planning/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").value("OK"));
    }

    private PlanningRequest createTestRequest() {
        UUID f1 = UUID.randomUUID();
        UUID t1 = UUID.randomUUID();
        UUID s1 = UUID.randomUUID();
        UUID p1 = UUID.randomUUID();

        FeatureRequest feature = new FeatureRequest(f1, "f1", "", 50.0);
        feature.setEffortEstimate(new EffortEstimate(40, 0, 0, 0));
        feature.addProductId(p1);

        Team team = new Team(t1, "Alpha");
        team.setDefaultCapacity(new Capacity(
            Map.of(Role.BACKEND, 160.0, Role.FRONTEND, 120.0, Role.QA, 80.0, Role.DEVOPS, 40.0),
            0.7, 0.20, 0.10
        ));

        Product product = new Product(p1, "Product A", "");
        product.addTeamId(t1);

        PlanningWindow window = new PlanningWindow(UUID.randomUUID(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        window.addSprint(new Sprint(s1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 14)));

        return new PlanningRequest(
            List.of(feature),
            List.of(team),
            List.of(product),
            window,
            PlanningParameters.defaults(),
            Set.of()
        );
    }

    private PlanningResult createTestResult() {
        UUID f1 = UUID.randomUUID();
        UUID t1 = UUID.randomUUID();
        UUID s1 = UUID.randomUUID();

        Assignment assignment = new Assignment(UUID.randomUUID(), f1, t1, s1);
        FeatureTimeline timeline = new FeatureTimeline(f1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 14), 0.85);

        return new PlanningResult(
            List.of(assignment),
            List.of(),
            Map.of(f1, timeline),
            Map.of(),
            80.0,
            100L,
            PlanningResult.PlanningAlgorithm.SIMULATED_ANNEALING
        );
    }
}
