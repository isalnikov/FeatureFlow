package com.featureflow.data.controller;

import com.featureflow.data.entity.SprintEntity;
import com.featureflow.data.repository.SprintRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sprints")
public class SprintController {

    private final SprintRepository sprintRepository;

    public SprintController(SprintRepository sprintRepository) {
        this.sprintRepository = sprintRepository;
    }

    @GetMapping
    public List<SprintEntity> list() {
        return sprintRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SprintEntity> get(@PathVariable UUID id) {
        return sprintRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SprintEntity> create(@RequestBody SprintEntity sprint) {
        sprint.setId(null);
        SprintEntity created = sprintRepository.save(sprint);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SprintEntity> update(@PathVariable UUID id, @RequestBody SprintEntity sprint) {
        if (!sprintRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        sprint.setId(id);
        SprintEntity updated = sprintRepository.save(sprint);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!sprintRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        sprintRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
