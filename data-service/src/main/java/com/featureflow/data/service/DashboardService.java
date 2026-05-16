package com.featureflow.data.service;

import com.featureflow.data.repository.AssignmentRepository;
import com.featureflow.data.repository.FeatureRepository;
import com.featureflow.data.repository.SprintRepository;
import com.featureflow.data.repository.TeamRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final JdbcTemplate jdbcTemplate;
    private final FeatureRepository featureRepo;
    private final AssignmentRepository assignmentRepo;
    private final SprintRepository sprintRepo;
    private final TeamRepository teamRepo;

    public DashboardService(JdbcTemplate jdbcTemplate,
                           FeatureRepository featureRepo,
                           AssignmentRepository assignmentRepo,
                           SprintRepository sprintRepo,
                           TeamRepository teamRepo) {
        this.jdbcTemplate = jdbcTemplate;
        this.featureRepo = featureRepo;
        this.assignmentRepo = assignmentRepo;
        this.sprintRepo = sprintRepo;
        this.teamRepo = teamRepo;
    }

    public List<Map<String, Object>> getTimeline() {
        return jdbcTemplate.queryForList("""
            SELECT f.id as featureId, f.title, f.business_value,
                   s.id as sprintId, s.start_date, s.end_date, s.label as sprintLabel,
                   t.id as teamId, t.name as teamName,
                   p.id as productId, p.name as productName
            FROM features f
            JOIN assignments a ON a.feature_id = f.id
            JOIN sprints s ON a.sprint_id = s.id
            JOIN teams t ON a.team_id = t.id
            JOIN feature_products fp ON fp.feature_id = f.id
            JOIN products p ON fp.product_id = p.id
            ORDER BY s.start_date, f.business_value DESC
            """);
    }

    public List<Map<String, Object>> getConflicts() {
        return jdbcTemplate.queryForList("""
            SELECT t.id as teamId, t.name as teamName,
                   s.id as sprintId, s.label as sprintLabel,
                   COUNT(a.id) as assignmentCount
            FROM teams t
            JOIN assignments a ON a.team_id = t.id
            JOIN sprints s ON a.sprint_id = s.id
            GROUP BY t.id, t.name, s.id, s.label
            HAVING COUNT(a.id) > 3
            """);
    }

    public List<Map<String, Object>> getTeamCapacityView() {
        return jdbcTemplate.queryForList("SELECT * FROM v_team_capacity");
    }

    public List<Map<String, Object>> getFeatureSummaryView() {
        return jdbcTemplate.queryForList("SELECT * FROM v_feature_summary");
    }

    public List<Map<String, Object>> getSprintLoadView() {
        return jdbcTemplate.queryForList("SELECT * FROM v_sprint_load");
    }
}
