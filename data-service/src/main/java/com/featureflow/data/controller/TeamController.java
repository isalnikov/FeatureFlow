package com.featureflow.data.controller;

import com.featureflow.data.dto.CreateTeamRequest;
import com.featureflow.data.dto.TeamDto;
import com.featureflow.data.dto.UpdateTeamRequest;
import com.featureflow.data.service.TeamService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {

    private final TeamService service;

    public TeamController(TeamService service) {
        this.service = service;
    }

    @GetMapping
    public List<TeamDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDto> get(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(service.getById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<TeamDto> create(@RequestBody CreateTeamRequest request) {
        TeamDto created = service.create(request);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamDto> update(@PathVariable UUID id, @RequestBody UpdateTeamRequest request) {
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
}
