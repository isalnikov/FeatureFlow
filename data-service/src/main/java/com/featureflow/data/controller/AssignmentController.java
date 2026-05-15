package com.featureflow.data.controller;

import com.featureflow.data.entity.AssignmentEntity;
import com.featureflow.data.repository.AssignmentRepository;
import com.featureflow.domain.valueobject.AssignmentStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assignments")

public class AssignmentController {

    private final AssignmentRepository repository;

    public AssignmentController(AssignmentRepository repository) {
        this.repository = repository;
    }
    @GetMapping
    public List<AssignmentEntity> list(
        @RequestParam(required = false) UUID teamId,
        @RequestParam(required = false) UUID sprintId,
        @RequestParam(required = false) AssignmentStatus status
    ) {
        if (teamId != null && sprintId != null) return repository.findByTeamAndSprint(teamId, sprintId);
        if (teamId != null) return repository.findByTeamId(teamId);
        if (sprintId != null) return repository.findBySprintId(sprintId);
        if (status != null) return repository.findByStatus(status);
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<AssignmentEntity> create(@RequestBody AssignmentEntity entity) {
        entity.setId(null);
        return ResponseEntity.status(201).body(repository.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssignmentEntity> update(@PathVariable UUID id, @RequestBody AssignmentEntity entity) {
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

    @PutMapping("/{id}/lock")
    public ResponseEntity<AssignmentEntity> lock(@PathVariable UUID id) {
        return repository.findById(id).map(a -> {
            a.setStatus(AssignmentStatus.LOCKED);
            return ResponseEntity.ok(repository.save(a));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<AssignmentEntity> unlock(@PathVariable UUID id) {
        return repository.findById(id).map(a -> {
            a.setStatus(AssignmentStatus.PLANNED);
            return ResponseEntity.ok(repository.save(a));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/bulk")
    public List<AssignmentEntity> bulkUpdate(@RequestBody List<AssignmentEntity> entities) {
        return repository.saveAll(entities);
    }
}
