package com.featureflow.data.controller;

import com.featureflow.data.entity.TeamEntity;
import com.featureflow.data.repository.TeamRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teams")

public class TeamController {

    private final TeamRepository repository;

    public TeamController(TeamRepository repository) {
        this.repository = repository;
    }
    @GetMapping
    public List<TeamEntity> list() { return repository.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<TeamEntity> get(@PathVariable UUID id) {
        return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TeamEntity> create(@RequestBody TeamEntity entity) {
        entity.setId(null);
        return ResponseEntity.status(201).body(repository.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamEntity> update(@PathVariable UUID id, @RequestBody TeamEntity entity) {
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
}
