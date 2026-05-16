package com.featureflow.data.controller;

import com.featureflow.data.dto.*;
import com.featureflow.data.service.AssignmentService;
import com.featureflow.domain.valueobject.AssignmentStatus;
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

@WebMvcTest(AssignmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssignmentService service;

    @Test
    void list_all_shouldReturnAll() throws Exception {
        AssignmentDto dto = assignmentDto();
        when(service.listAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/assignments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(dto.getId().toString()));
    }

    @Test
    void list_byTeamId_shouldFilter() throws Exception {
        UUID teamId = UUID.randomUUID();
        AssignmentDto dto = assignmentDto();
        when(service.findByTeamId(teamId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/assignments")
                .param("teamId", teamId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(dto.getId().toString()));
    }

    @Test
    void list_bySprintId_shouldFilter() throws Exception {
        UUID sprintId = UUID.randomUUID();
        AssignmentDto dto = assignmentDto();
        when(service.findBySprintId(sprintId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/assignments")
                .param("sprintId", sprintId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(dto.getId().toString()));
    }

    @Test
    void list_byStatus_shouldFilter() throws Exception {
        AssignmentDto dto = assignmentDto();
        when(service.findByStatus(AssignmentStatus.PLANNED)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/assignments")
                .param("status", "PLANNED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(dto.getId().toString()));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        CreateAssignmentRequest request = new CreateAssignmentRequest();
        request.setFeatureId(UUID.randomUUID());
        request.setTeamId(UUID.randomUUID());
        request.setSprintId(UUID.randomUUID());

        AssignmentDto dto = assignmentDto();
        when(service.create(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(dto.getId().toString()));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateAssignmentRequest request = new UpdateAssignmentRequest();
        request.setAllocatedEffort(Map.of("backendHours", 40.0));

        AssignmentDto dto = assignmentDto();
        dto.setId(id);
        when(service.update(eq(id), any())).thenReturn(dto);

        mockMvc.perform(put("/api/v1/assignments/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void update_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateAssignmentRequest request = new UpdateAssignmentRequest();
        when(service.update(eq(id), any())).thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));

        mockMvc.perform(put("/api/v1/assignments/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/api/v1/assignments/{id}", id))
            .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new jakarta.persistence.EntityNotFoundException("Not found")).when(service).delete(id);

        mockMvc.perform(delete("/api/v1/assignments/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void lock_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        AssignmentDto dto = assignmentDto();
        dto.setId(id);
        dto.setStatus(AssignmentStatus.LOCKED);
        when(service.lock(id)).thenReturn(dto);

        mockMvc.perform(put("/api/v1/assignments/{id}/lock", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("LOCKED"));
    }

    @Test
    void lock_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.lock(id)).thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));

        mockMvc.perform(put("/api/v1/assignments/{id}/lock", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void unlock_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        AssignmentDto dto = assignmentDto();
        dto.setId(id);
        dto.setStatus(AssignmentStatus.PLANNED);
        when(service.unlock(id)).thenReturn(dto);

        mockMvc.perform(put("/api/v1/assignments/{id}/unlock", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PLANNED"));
    }

    @Test
    void bulkCreate_shouldReturn201() throws Exception {
        CreateAssignmentRequest req = new CreateAssignmentRequest();
        req.setFeatureId(UUID.randomUUID());
        req.setTeamId(UUID.randomUUID());
        req.setSprintId(UUID.randomUUID());

        AssignmentDto dto = assignmentDto();
        when(service.bulkCreate(any())).thenReturn(List.of(dto));

        mockMvc.perform(post("/api/v1/assignments/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(req))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$[0].id").value(dto.getId().toString()));
    }

    private AssignmentDto assignmentDto() {
        AssignmentDto dto = new AssignmentDto();
        dto.setId(UUID.randomUUID());
        dto.setFeatureId(UUID.randomUUID());
        dto.setTeamId(UUID.randomUUID());
        dto.setSprintId(UUID.randomUUID());
        dto.setStatus(AssignmentStatus.PLANNED);
        dto.setVersion(1);
        dto.setCreatedAt(Instant.now());
        dto.setUpdatedAt(Instant.now());
        return dto;
    }
}
