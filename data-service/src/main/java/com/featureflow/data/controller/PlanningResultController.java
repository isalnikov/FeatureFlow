package com.featureflow.data.controller;

import com.featureflow.data.dto.PlanningResultSummary;
import com.featureflow.data.dto.SavePlanningResultRequest;
import com.featureflow.data.entity.PlanningResultEntity;
import com.featureflow.data.service.PlanningResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/planning-results")
public class PlanningResultController {

    private final PlanningResultService planningResultService;

    public PlanningResultController(PlanningResultService planningResultService) {
        this.planningResultService = planningResultService;
    }

    @PostMapping
    public ResponseEntity<Void> saveResult(@RequestBody SavePlanningResultRequest request) {
        planningResultService.save(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<PlanningResultSummary>> listResults(
        @RequestParam(required = false) String algorithm,
        @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(planningResultService.listRecent(algorithm, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanningResultEntity> getResult(@PathVariable UUID id) {
        return planningResultService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResult(@PathVariable UUID id) {
        planningResultService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
