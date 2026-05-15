package com.featureflow.data.controller;

import com.featureflow.data.entity.FeatureEntity;
import com.featureflow.data.repository.FeatureRepository;
import com.featureflow.domain.valueobject.ClassOfService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/features")

public class FeatureController {

    private final FeatureRepository repository;

    public FeatureController(FeatureRepository repository) {
        this.repository = repository;
    }
    @GetMapping
    public Page<FeatureEntity> list(
        @RequestParam(required = false) ClassOfService classOfService,
        Pageable pageable
    ) {
        if (classOfService != null) {
            return repository.findByClassOfService(classOfService, pageable);
        }
        return repository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeatureEntity> get(@PathVariable UUID id) {
        return repository.findByIdWithProducts(id) != null
            ? ResponseEntity.ok(repository.findByIdWithProducts(id))
            : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<FeatureEntity> create(@RequestBody FeatureEntity entity) {
        entity.setId(null);
        return ResponseEntity.status(201).body(repository.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeatureEntity> update(@PathVariable UUID id, @RequestBody FeatureEntity entity) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        entity.setId(id);
        return ResponseEntity.ok(repository.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    public List<FeatureEntity> bulkCreate(@RequestBody List<FeatureEntity> entities) {
        entities.forEach(e -> e.setId(null));
        return repository.saveAll(entities);
    }
}
