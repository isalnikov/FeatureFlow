package com.featureflow.data.controller;

import com.featureflow.data.dto.CreateSprintRequest;
import com.featureflow.data.dto.SprintDto;
import com.featureflow.data.dto.UpdateSprintRequest;
import com.featureflow.data.service.SprintService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sprints")
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @GetMapping
    public ResponseEntity<List<SprintDto>> list(
        @RequestParam(required = false) UUID planningWindowId
    ) {
        List<SprintDto> sprints;
        if (planningWindowId != null) {
            sprints = sprintService.listByPlanningWindow(planningWindowId);
        } else {
            sprints = List.of();
        }
        return ResponseEntity.ok(sprints);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SprintDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(sprintService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SprintDto> create(
        @RequestParam UUID planningWindowId,
        @RequestBody CreateSprintRequest request
    ) {
        SprintDto created = sprintService.create(planningWindowId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SprintDto> update(
        @PathVariable UUID id,
        @RequestBody UpdateSprintRequest request
    ) {
        return ResponseEntity.ok(sprintService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        sprintService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
