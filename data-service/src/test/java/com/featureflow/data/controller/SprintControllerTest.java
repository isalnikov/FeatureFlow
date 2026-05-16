package com.featureflow.data.controller;

import com.featureflow.data.dto.CreateSprintRequest;
import com.featureflow.data.dto.SprintDto;
import com.featureflow.data.dto.UpdateSprintRequest;
import com.featureflow.data.service.SprintService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SprintController.class)
@AutoConfigureMockMvc(addFilters = false)
class SprintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SprintService sprintService;

    @Test
    void list_withPlanningWindowId_shouldReturnSprints() throws Exception {
        UUID pwId = UUID.randomUUID();
        SprintDto dto = new SprintDto(
            UUID.randomUUID(),
            null,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 14),
            null,
            null
        );

        when(sprintService.listByPlanningWindow(pwId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/sprints")
                .param("planningWindowId", pwId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].startDate").value("2025-01-01"));
    }

    @Test
    void get_shouldReturnSprint() throws Exception {
        UUID id = UUID.randomUUID();
        SprintDto dto = new SprintDto(
            id,
            null,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 14),
            null,
            null
        );

        when(sprintService.getById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/sprints/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void create_shouldReturnCreated() throws Exception {
        UUID pwId = UUID.randomUUID();
        CreateSprintRequest request = new CreateSprintRequest(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 14), Map.of(), "SPR-1");

        SprintDto dto = new SprintDto(
            UUID.randomUUID(),
            pwId,
            LocalDate.of(2025, 2, 1),
            LocalDate.of(2025, 2, 14),
            null,
            null
        );

        when(sprintService.create(eq(pwId), any(CreateSprintRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/sprints")
                .param("planningWindowId", pwId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.startDate").value("2025-02-01"));
    }

    @Test
    void update_shouldReturnUpdated() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateSprintRequest request = new UpdateSprintRequest(null, LocalDate.of(2025, 1, 21), null, null);

        SprintDto dto = new SprintDto(
            id,
            null,
            null,
            LocalDate.of(2025, 1, 21),
            null,
            null
        );

        when(sprintService.update(eq(id), any(UpdateSprintRequest.class))).thenReturn(dto);

        mockMvc.perform(put("/api/v1/sprints/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.endDate").value("2025-01-21"));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(sprintService).delete(id);

        mockMvc.perform(delete("/api/v1/sprints/{id}", id))
            .andExpect(status().isNoContent());
    }
}
