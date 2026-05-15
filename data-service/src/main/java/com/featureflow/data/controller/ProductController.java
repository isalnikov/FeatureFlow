package com.featureflow.data.controller;

import com.featureflow.data.dto.CreateProductRequest;
import com.featureflow.data.dto.ProductDto;
import com.featureflow.data.dto.TeamDto;
import com.featureflow.data.dto.UpdateProductRequest;
import com.featureflow.data.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> get(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(service.getById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@RequestBody CreateProductRequest request) {
        ProductDto created = service.create(request);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(@PathVariable UUID id, @RequestBody UpdateProductRequest request) {
        try {
            return ResponseEntity.ok(service.update(id, request));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/teams")
    public ResponseEntity<List<TeamDto>> getWithTeams(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(service.getTeams(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
