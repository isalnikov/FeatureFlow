package com.featureflow.data.controller;

import com.featureflow.data.entity.PlanningWindowEntity;
import com.featureflow.data.entity.SprintEntity;
import com.featureflow.data.repository.PlanningWindowRepository;
import com.featureflow.data.repository.SprintRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/planning-windows")
public class PlanningWindowController {

    private final PlanningWindowRepository windowRepo;
    private final SprintRepository sprintRepo;

    public PlanningWindowController(PlanningWindowRepository windowRepo, SprintRepository sprintRepo) {
        this.windowRepo = windowRepo;
        this.sprintRepo = sprintRepo;
    }

    @GetMapping
    public List<PlanningWindowEntity> list() { return windowRepo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<PlanningWindowEntity> get(@PathVariable UUID id) {
        return windowRepo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PlanningWindowEntity> create(@RequestBody PlanningWindowEntity entity) {
        entity.setId(null);
        return ResponseEntity.status(201).body(windowRepo.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanningWindowEntity> update(@PathVariable UUID id, @RequestBody PlanningWindowEntity entity) {
        if (!windowRepo.existsById(id)) return ResponseEntity.notFound().build();
        entity.setId(id);
        return ResponseEntity.ok(windowRepo.save(entity));
    }

    @GetMapping("/{id}/sprints")
    public List<SprintEntity> getSprints(@PathVariable UUID id) {
        return sprintRepo.findByPlanningWindowId(id);
    }

    @PostMapping("/{id}/sprints")
    public ResponseEntity<SprintEntity> addSprint(@PathVariable UUID id, @RequestBody SprintEntity entity) {
        if (!windowRepo.existsById(id)) return ResponseEntity.notFound().build();
        entity.setId(null);
        return ResponseEntity.status(201).body(sprintRepo.save(entity));
    }

    @PutMapping("/sprints/{sprintId}")
    public ResponseEntity<SprintEntity> updateSprint(@PathVariable UUID sprintId, @RequestBody SprintEntity entity) {
        if (!sprintRepo.existsById(sprintId)) return ResponseEntity.notFound().build();
        entity.setId(sprintId);
        return ResponseEntity.ok(sprintRepo.save(entity));
    }
}
