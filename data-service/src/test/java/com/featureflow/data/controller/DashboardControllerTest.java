package com.featureflow.data.controller;

import com.featureflow.data.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeatureRepository featureRepo;

    @MockitoBean
    private TeamRepository teamRepo;

    @MockitoBean
    private AssignmentRepository assignmentRepo;

    @Test
    void portfolio_shouldReturnCounts() throws Exception {
        when(featureRepo.count()).thenReturn(10L);
        when(teamRepo.count()).thenReturn(3L);
        when(assignmentRepo.findDistinctActiveFeatureIds()).thenReturn(List.of(UUID.randomUUID(), UUID.randomUUID()));

        mockMvc.perform(get("/api/v1/dashboard/portfolio"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalFeatures").value(10))
            .andExpect(jsonPath("$.plannedFeatures").value(2))
            .andExpect(jsonPath("$.totalTeams").value(3));
    }

    @Test
    void teamLoad_shouldReturnLoad() throws Exception {
        when(teamRepo.findAll()).thenReturn(List.of());
        when(assignmentRepo.count()).thenReturn(5L);

        mockMvc.perform(get("/api/v1/dashboard/team-load"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.teams").value(0))
            .andExpect(jsonPath("$.assignments").value(5));
    }

    @Test
    void conflicts_shouldReturnEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/conflicts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(0))
            .andExpect(jsonPath("$.list").isEmpty());
    }

    @Test
    void metrics_shouldReturnMetrics() throws Exception {
        when(featureRepo.count()).thenReturn(10L);
        when(teamRepo.count()).thenReturn(3L);
        when(assignmentRepo.count()).thenReturn(5L);

        mockMvc.perform(get("/api/v1/dashboard/metrics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.features").value(10))
            .andExpect(jsonPath("$.teams").value(3))
            .andExpect(jsonPath("$.assignments").value(5));
    }

    @Test
    void timeline_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/timeline"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }
}
