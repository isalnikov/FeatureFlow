package com.featureflow.data.controller;

import com.featureflow.data.dto.CreatePlanningWindowRequest;
import com.featureflow.data.dto.CreateSprintRequest;
import com.featureflow.data.dto.PlanningWindowDto;
import com.featureflow.data.dto.SprintDto;
import com.featureflow.data.service.PlanningWindowService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/planning-windows")
public class PlanningWindowController {

    private final PlanningWindowService service;

    public PlanningWindowController(PlanningWindowService service) {
        this.service = service;
    }

    @GetMapping
    public List<PlanningWindowDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanningWindowDto> get(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(service.getById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<PlanningWindowDto> create(@RequestBody CreatePlanningWindowRequest request) {
        PlanningWindowDto created = service.create(request);
        return ResponseEntity.status(201).body(created);
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

    @GetMapping("/{id}/sprints")
    public ResponseEntity<List<SprintDto>> getSprints(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(service.getSprints(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/sprints")
    public ResponseEntity<SprintDto> addSprint(@PathVariable UUID id, @RequestBody CreateSprintRequest request) {
        try {
            SprintDto created = service.addSprint(id, request);
            return ResponseEntity.status(201).body(created);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
