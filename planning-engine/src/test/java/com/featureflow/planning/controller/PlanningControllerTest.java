package com.featureflow.planning.controller;

import com.featureflow.planning.model.PlanningRunRequest;
import com.featureflow.planning.service.PlanningOrchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlanningController.class)
class PlanningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlanningOrchestrator orchestrator;

    @Test
    void runPlanning_acceptsRequest_returnsAccepted() throws Exception {
        PlanningRunRequest request = new PlanningRunRequest(
            List.of(UUID.randomUUID()),
            List.of(UUID.randomUUID()),
            UUID.randomUUID(),
            null,
            Set.of()
        );

        mockMvc.perform(post("/api/v1/planning/run")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(header().exists("X-Job-Id"))
            .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }
}
