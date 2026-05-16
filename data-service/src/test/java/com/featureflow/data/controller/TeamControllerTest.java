package com.featureflow.data.controller;

import com.featureflow.data.dto.*;
import com.featureflow.data.service.TeamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeamController.class)
@AutoConfigureMockMvc(addFilters = false)
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TeamService service;

    @Test
    void list_shouldReturnTeams() throws Exception {
        TeamDto dto = teamDto("Team Alpha");
        when(service.listAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/teams"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Team Alpha"));
    }

    @Test
    void get_shouldReturnTeam() throws Exception {
        UUID id = UUID.randomUUID();
        TeamDto dto = teamDto("Team Alpha");
        dto.setId(id);
        when(service.getById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/teams/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.name").value("Team Alpha"));
    }

    @Test
    void get_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/teams/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201() throws Exception {
        CreateTeamRequest request = new CreateTeamRequest();
        request.setName("New Team");
        request.setFocusFactor(0.8);
        request.setBugReservePercent(0.15);
        request.setTechDebtReservePercent(0.1);
        request.setExpertiseTags(Set.of("java"));
        request.setExternalId("ext-1");

        TeamDto dto = teamDto("New Team");
        when(service.create(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("New Team"));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateTeamRequest request = new UpdateTeamRequest();
        request.setName("Updated Team");
        request.setFocusFactor(0.85);
        request.setVelocity(60.0);
        request.setExpertiseTags(Set.of("spring"));

        TeamDto dto = teamDto("Updated Team");
        dto.setId(id);
        when(service.update(eq(id), any())).thenReturn(dto);

        mockMvc.perform(put("/api/v1/teams/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Team"));
    }

    @Test
    void update_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateTeamRequest request = new UpdateTeamRequest();
        request.setName("Updated");
        when(service.update(eq(id), any())).thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));

        mockMvc.perform(put("/api/v1/teams/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/api/v1/teams/{id}", id))
            .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new jakarta.persistence.EntityNotFoundException("Not found")).when(service).delete(id);

        mockMvc.perform(delete("/api/v1/teams/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void capacity_shouldReturnCapacity() throws Exception {
        UUID id = UUID.randomUUID();
        UUID sprintId = UUID.randomUUID();
        Map<String, Double> capacity = Map.of("backendHours", 160.0, "frontendHours", 120.0);
        when(service.getCapacityForSprint(id, sprintId)).thenReturn(capacity);

        mockMvc.perform(get("/api/v1/teams/{id}/capacity", id)
                .param("sprintId", sprintId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.backendHours").value(160.0));
    }

    @Test
    void capacity_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        UUID sprintId = UUID.randomUUID();
        when(service.getCapacityForSprint(id, sprintId)).thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/teams/{id}/capacity", id)
                .param("sprintId", sprintId.toString()))
            .andExpect(status().isNotFound());
    }

    @Test
    void load_shouldReturnLoad() throws Exception {
        UUID id = UUID.randomUUID();
        UUID sprintId = UUID.randomUUID();
        when(service.getTeamLoad(id, sprintId)).thenReturn(85.5);

        mockMvc.perform(get("/api/v1/teams/{id}/load", id)
                .param("sprintId", sprintId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(85.5));
    }

    @Test
    void load_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        UUID sprintId = UUID.randomUUID();
        when(service.getTeamLoad(id, sprintId)).thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/teams/{id}/load", id)
                .param("sprintId", sprintId.toString()))
            .andExpect(status().isNotFound());
    }

    private TeamDto teamDto(String name) {
        TeamDto dto = new TeamDto();
        dto.setId(UUID.randomUUID());
        dto.setName(name);
        dto.setFocusFactor(0.7);
        dto.setBugReservePercent(0.2);
        dto.setTechDebtReservePercent(0.1);
        dto.setVelocity(50.0);
        dto.setExpertiseTags(Set.of("java"));
        dto.setExternalId("ext-1");
        dto.setVersion(1);
        dto.setCreatedAt(Instant.now());
        dto.setUpdatedAt(Instant.now());
        dto.setCreatedBy("user");
        dto.setUpdatedBy("user");
        return dto;
    }
}
