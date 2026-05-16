package com.featureflow.data.controller;

import com.featureflow.data.dto.*;
import com.featureflow.data.service.PlanningWindowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlanningWindowController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlanningWindowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlanningWindowService service;

    @Test
    void list_shouldReturnWindows() throws Exception {
        PlanningWindowDto dto = planningWindowDto();
        when(service.listAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/planning-windows"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(dto.getId().toString()));
    }

    @Test
    void get_shouldReturnWindow() throws Exception {
        UUID id = UUID.randomUUID();
        PlanningWindowDto dto = planningWindowDto();
        dto.setId(id);
        when(service.getById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/planning-windows/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void get_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/planning-windows/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201() throws Exception {
        CreatePlanningWindowRequest request = new CreatePlanningWindowRequest();
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setEndDate(LocalDate.of(2025, 6, 30));

        PlanningWindowDto dto = planningWindowDto();
        when(service.create(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/planning-windows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(dto.getId().toString()));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/api/v1/planning-windows/{id}", id))
            .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new jakarta.persistence.EntityNotFoundException("Not found")).when(service).delete(id);

        mockMvc.perform(delete("/api/v1/planning-windows/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void getSprints_shouldReturnSprints() throws Exception {
        UUID id = UUID.randomUUID();
        SprintDto sprintDto = sprintDto();
        when(service.getSprints(id)).thenReturn(List.of(sprintDto));

        mockMvc.perform(get("/api/v1/planning-windows/{id}/sprints", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(sprintDto.getId().toString()));
    }

    @Test
    void getSprints_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getSprints(id)).thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/planning-windows/{id}/sprints", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void addSprint_shouldReturn201() throws Exception {
        UUID id = UUID.randomUUID();
        CreateSprintRequest request = new CreateSprintRequest();
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setEndDate(LocalDate.of(2025, 1, 14));

        SprintDto sprintDto = sprintDto();
        when(service.addSprint(eq(id), any())).thenReturn(sprintDto);

        mockMvc.perform(post("/api/v1/planning-windows/{id}/sprints", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(sprintDto.getId().toString()));
    }

    @Test
    void addSprint_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        CreateSprintRequest request = new CreateSprintRequest();
        when(service.addSprint(eq(id), any())).thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));

        mockMvc.perform(post("/api/v1/planning-windows/{id}/sprints", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    private PlanningWindowDto planningWindowDto() {
        PlanningWindowDto dto = new PlanningWindowDto();
        dto.setId(UUID.randomUUID());
        dto.setStartDate(LocalDate.of(2025, 1, 1));
        dto.setEndDate(LocalDate.of(2025, 6, 30));
        dto.setVersion(1);
        dto.setCreatedAt(Instant.now());
        return dto;
    }

    private SprintDto sprintDto() {
        SprintDto dto = new SprintDto();
        dto.setId(UUID.randomUUID());
        dto.setStartDate(LocalDate.of(2025, 1, 1));
        dto.setEndDate(LocalDate.of(2025, 1, 14));
        return dto;
    }
}
