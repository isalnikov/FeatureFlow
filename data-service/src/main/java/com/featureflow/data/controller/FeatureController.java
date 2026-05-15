package com.featureflow.data.controller;

import com.featureflow.data.dto.CreateFeatureRequest;
import com.featureflow.data.dto.FeatureDto;
import com.featureflow.data.dto.UpdateFeatureRequest;
import com.featureflow.data.service.FeatureService;
import com.featureflow.domain.valueobject.ClassOfService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/features")
public class FeatureController {

    private final FeatureService service;

    public FeatureController(FeatureService service) {
        this.service = service;
    }

    @GetMapping
    public Page<FeatureDto> list(
        @RequestParam(required = false) ClassOfService classOfService,
        Pageable pageable
    ) {
        return service.listAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeatureDto> get(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(service.getById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<FeatureDto> create(@RequestBody CreateFeatureRequest request) {
        FeatureDto created = service.create(request);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeatureDto> update(@PathVariable UUID id, @RequestBody UpdateFeatureRequest request) {
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

    @PostMapping("/bulk")
    public ResponseEntity<List<FeatureDto>> bulkCreate(@RequestBody List<CreateFeatureRequest> requests) {
        return ResponseEntity.status(201).body(service.bulkCreate(requests));
    }

    @GetMapping("/{id}/deps")
    public ResponseEntity<Set<UUID>> getDependencies(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(service.getDependencies(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/deps")
    public ResponseEntity<Void> updateDependencies(
            @PathVariable UUID id,
            @RequestBody Set<UUID> dependencies) {
        try {
            service.updateDependencies(id, dependencies);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
