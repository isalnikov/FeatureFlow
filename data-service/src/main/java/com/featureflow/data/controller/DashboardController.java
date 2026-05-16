package com.featureflow.data.controller;

import com.featureflow.data.repository.*;
import com.featureflow.data.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final FeatureRepository featureRepo;
    private final TeamRepository teamRepo;
    private final AssignmentRepository assignmentRepo;
    private final DashboardService dashboardService;

    public DashboardController(FeatureRepository featureRepo, TeamRepository teamRepo,
                               AssignmentRepository assignmentRepo, DashboardService dashboardService) {
        this.featureRepo = featureRepo;
        this.teamRepo = teamRepo;
        this.assignmentRepo = assignmentRepo;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/portfolio")
    public Map<String, Object> portfolio() {
        long totalFeatures = featureRepo.count();
        long plannedFeatures = assignmentRepo.findDistinctActiveFeatureIds().size();
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
        List<Map<String, Object>> conflicts = dashboardService.getConflicts();
        return Map.of("active", conflicts.size(), "list", conflicts);
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        return Map.of(
            "features", featureRepo.count(),
            "teams", teamRepo.count(),
            "assignments", assignmentRepo.count()
        );
    }

    @GetMapping("/timeline")
    public List<Map<String, Object>> timeline() {
        return dashboardService.getTimeline();
    }

    @GetMapping("/views/team-capacity")
    public List<Map<String, Object>> getTeamCapacityView() {
        return dashboardService.getTeamCapacityView();
    }

    @GetMapping("/views/feature-summary")
    public List<Map<String, Object>> getFeatureSummaryView() {
        return dashboardService.getFeatureSummaryView();
    }

    @GetMapping("/views/sprint-load")
    public List<Map<String, Object>> getSprintLoadView() {
        return dashboardService.getSprintLoadView();
    }
}
