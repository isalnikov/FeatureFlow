package com.featureflow.data.controller;

import com.featureflow.data.dto.AssignmentDto;
import com.featureflow.data.dto.CreateAssignmentRequest;
import com.featureflow.data.dto.UpdateAssignmentRequest;
import com.featureflow.data.service.AssignmentService;
import com.featureflow.domain.valueobject.AssignmentStatus;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {

    private final AssignmentService service;

    public AssignmentController(AssignmentService service) {
        this.service = service;
    }

    @GetMapping
    public List<AssignmentDto> list(
        @RequestParam(required = false) UUID teamId,
        @RequestParam(required = false) UUID sprintId,
        @RequestParam(required = false) AssignmentStatus status
    ) {
        if (teamId != null) {
            return service.findByTeamId(teamId);
        }
        if (sprintId != null) {
            return service.findBySprintId(sprintId);
        }
        if (status != null) {
            return service.findByStatus(status);
        }
        return service.listAll();
    }

    @PostMapping
    public ResponseEntity<AssignmentDto> create(@RequestBody CreateAssignmentRequest request) {
        AssignmentDto created = service.create(request);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssignmentDto> update(@PathVariable UUID id, @RequestBody UpdateAssignmentRequest request) {
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

    @PutMapping("/{id}/lock")
    public ResponseEntity<AssignmentDto> lock(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(service.lock(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<AssignmentDto> unlock(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(service.unlock(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<AssignmentDto>> bulkCreate(@RequestBody List<CreateAssignmentRequest> requests) {
        return ResponseEntity.status(201).body(service.bulkCreate(requests));
    }
}
