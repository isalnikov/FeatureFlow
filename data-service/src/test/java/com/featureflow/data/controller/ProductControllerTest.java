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
        dto = new ProductDto(
            id,
            dto.name(),
            dto.description(),
            dto.technologyStack(),
            dto.version(),
            dto.createdAt(),
            dto.updatedAt(),
            dto.createdBy(),
            dto.updatedBy()
        );
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
        CreateProductRequest request = new CreateProductRequest("New Product", "Description", Set.of("java"));

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
        UpdateProductRequest request = new UpdateProductRequest("Updated", "New desc", Set.of("spring"));

        ProductDto dto = productDto("Updated");
        dto = new ProductDto(
            id,
            dto.name(),
            dto.description(),
            dto.technologyStack(),
            dto.version(),
            dto.createdAt(),
            dto.updatedAt(),
            dto.createdBy(),
            dto.updatedBy()
        );
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
        UpdateProductRequest request = new UpdateProductRequest("Updated", null, null);
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
        TeamDto teamDto = new TeamDto(
            id,
            "Team Alpha",
            null, null, null, null, null, null, null, null, null, null, null
        );
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
        return new ProductDto(
            UUID.randomUUID(),
            name,
            "Desc",
            Set.of("java"),
            1,
            Instant.now(),
            Instant.now(),
            "user",
            "user"
        );
    }
}
