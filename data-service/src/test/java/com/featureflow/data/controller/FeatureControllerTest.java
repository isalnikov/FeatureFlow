package com.featureflow.data.controller;

import com.featureflow.data.dto.*;
import com.featureflow.data.service.FeatureService;
import com.featureflow.domain.valueobject.ClassOfService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FeatureController.class)
@AutoConfigureMockMvc(addFilters = false)
class FeatureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FeatureService service;

    @Test
    void list_shouldReturnPageOfFeatures() throws Exception {
        FeatureDto dto = featureDto("Feature A");
        when(service.listAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/v1/features")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value("Feature A"));
    }

    @Test
    void get_shouldReturnFeature() throws Exception {
        UUID id = UUID.randomUUID();
        FeatureDto dto = featureDto("Feature A");
        dto.setId(id);
        when(service.getById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/features/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.title").value("Feature A"));
    }

    @Test
    void get_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/features/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201() throws Exception {
        CreateFeatureRequest request = new CreateFeatureRequest();
        request.setTitle("New Feature");
        request.setBusinessValue(50.0);

        FeatureDto dto = featureDto("New Feature");
        when(service.create(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/features")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("New Feature"));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateFeatureRequest request = new UpdateFeatureRequest();
        request.setTitle("Updated Feature");

        FeatureDto dto = featureDto("Updated Feature");
        dto.setId(id);
        when(service.update(eq(id), any())).thenReturn(dto);

        mockMvc.perform(put("/api/v1/features/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Updated Feature"));
    }

    @Test
    void update_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateFeatureRequest request = new UpdateFeatureRequest();
        request.setTitle("Updated");
        when(service.update(eq(id), any())).thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));

        mockMvc.perform(put("/api/v1/features/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/api/v1/features/{id}", id))
            .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new jakarta.persistence.EntityNotFoundException("Not found")).when(service).delete(id);

        mockMvc.perform(delete("/api/v1/features/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void bulkCreate_shouldReturn201() throws Exception {
        CreateFeatureRequest req = new CreateFeatureRequest();
        req.setTitle("Feature 1");
        req.setBusinessValue(50.0);

        FeatureDto dto = featureDto("Feature 1");
        when(service.bulkCreate(any())).thenReturn(List.of(dto));

        mockMvc.perform(post("/api/v1/features/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(req))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$[0].title").value("Feature 1"));
    }

    @Test
    void getDependencies_shouldReturnDeps() throws Exception {
        UUID id = UUID.randomUUID();
        UUID depId = UUID.randomUUID();
        when(service.getDependencies(id)).thenReturn(Set.of(depId));

        mockMvc.perform(get("/api/v1/features/{id}/deps", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").value(depId.toString()));
    }

    @Test
    void getDependencies_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getDependencies(id)).thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/features/{id}/deps", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateDependencies_shouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();
        Set<UUID> deps = Set.of(UUID.randomUUID());
        doNothing().when(service).updateDependencies(eq(id), any());

        mockMvc.perform(put("/api/v1/features/{id}/deps", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deps)))
            .andExpect(status().isNoContent());
    }

    @Test
    void updateDependencies_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        Set<UUID> deps = Set.of(UUID.randomUUID());
        doThrow(new jakarta.persistence.EntityNotFoundException("Not found")).when(service).updateDependencies(eq(id), any());

        mockMvc.perform(put("/api/v1/features/{id}/deps", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deps)))
            .andExpect(status().isNotFound());
    }

    private FeatureDto featureDto(String title) {
        FeatureDto dto = new FeatureDto();
        dto.setId(UUID.randomUUID());
        dto.setTitle(title);
        dto.setDescription("Desc");
        dto.setBusinessValue(50.0);
        dto.setClassOfService(ClassOfService.STANDARD);
        dto.setCanSplit(false);
        dto.setVersion(1);
        dto.setCreatedAt(Instant.now());
        dto.setUpdatedAt(Instant.now());
        return dto;
    }
}
