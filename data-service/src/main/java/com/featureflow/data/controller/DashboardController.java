package com.featureflow.data.controller;

import com.featureflow.data.repository.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final FeatureRepository featureRepo;
    private final TeamRepository teamRepo;
    private final AssignmentRepository assignmentRepo;

    public DashboardController(FeatureRepository featureRepo, TeamRepository teamRepo, AssignmentRepository assignmentRepo) {
        this.featureRepo = featureRepo;
        this.teamRepo = teamRepo;
        this.assignmentRepo = assignmentRepo;
    }

    @GetMapping("/portfolio")
    public Map<String, Object> portfolio() {
        long totalFeatures = featureRepo.count();
        long plannedFeatures = assignmentRepo.findAll().stream()
            .map(a -> a.getFeature().getId())
            .distinct()
            .count();
        return Map.of(
            "totalFeatures", totalFeatures,
            "plannedFeatures", plannedFeatures,
            "totalTeams", teamRepo.count()
        );
    }

    @GetMapping("/team-load")
    public Map<String, Object> teamLoad() {
        return Map.of("teams", teamRepo.findAll().size(), "assignments", assignmentRepo.count());
    }

    @GetMapping("/conflicts")
    public Map<String, Object> conflicts() {
        return Map.of("active", 0, "list", java.util.List.of());
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        return Map.of(
            "features", featureRepo.count(),
            "teams", teamRepo.count(),
            "assignments", assignmentRepo.count()
        );
    }
}
