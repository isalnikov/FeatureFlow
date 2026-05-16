package com.featureflow.data.controller;

import com.featureflow.data.dto.*;
import com.featureflow.data.service.ProductService;
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

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService service;

    @Test
    void list_shouldReturnProducts() throws Exception {
        ProductDto dto = productDto("Product A");
        when(service.listAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Product A"));
    }

    @Test
    void get_shouldReturnProduct() throws Exception {
        UUID id = UUID.randomUUID();
        ProductDto dto = productDto("Product A");
        dto.setId(id);
        when(service.getById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/products/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.name").value("Product A"));
    }

    @Test
    void get_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/products/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("New Product");
        request.setDescription("Description");
        request.setTechnologyStack(Set.of("java"));

        ProductDto dto = productDto("New Product");
        when(service.create(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("New Product"));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Updated");
        request.setDescription("New desc");
        request.setTechnologyStack(Set.of("spring"));

        ProductDto dto = productDto("Updated");
        dto.setId(id);
        when(service.update(eq(id), any())).thenReturn(dto);

        mockMvc.perform(put("/api/v1/products/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void update_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Updated");
        when(service.update(eq(id), any())).thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));

        mockMvc.perform(put("/api/v1/products/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/api/v1/products/{id}", id))
            .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new jakarta.persistence.EntityNotFoundException("Not found")).when(service).delete(id);

        mockMvc.perform(delete("/api/v1/products/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void getWithTeams_shouldReturnTeams() throws Exception {
        UUID id = UUID.randomUUID();
        TeamDto teamDto = new TeamDto();
        teamDto.setId(id);
        teamDto.setName("Team Alpha");
        when(service.getTeams(id)).thenReturn(List.of(teamDto));

        mockMvc.perform(get("/api/v1/products/{id}/teams", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Team Alpha"));
    }

    @Test
    void getWithTeams_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getTeams(id)).thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/products/{id}/teams", id))
            .andExpect(status().isNotFound());
    }

    private ProductDto productDto(String name) {
        ProductDto dto = new ProductDto();
        dto.setId(UUID.randomUUID());
        dto.setName(name);
        dto.setDescription("Desc");
        dto.setTechnologyStack(Set.of("java"));
        dto.setVersion(1);
        dto.setCreatedAt(Instant.now());
        dto.setUpdatedAt(Instant.now());
        dto.setCreatedBy("user");
        dto.setUpdatedBy("user");
        return dto;
    }
}
