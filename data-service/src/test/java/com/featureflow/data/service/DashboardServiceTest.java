package com.featureflow.data.service;

import com.featureflow.data.repository.AssignmentRepository;
import com.featureflow.data.repository.FeatureRepository;
import com.featureflow.data.repository.SprintRepository;
import com.featureflow.data.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private FeatureRepository featureRepo;
    @Mock
    private AssignmentRepository assignmentRepo;
    @Mock
    private SprintRepository sprintRepo;
    @Mock
    private TeamRepository teamRepo;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getTimeline_returnsJoinedData() {
        List<Map<String, Object>> expected = List.of(
            Map.of("featureId", "f1", "title", "Feature 1", "sprintLabel", "Sprint 1")
        );
        when(jdbcTemplate.queryForList(org.mockito.ArgumentMatchers.contains("SELECT f.id"))).thenReturn(expected);

        List<Map<String, Object>> result = dashboardService.getTimeline();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("title")).isEqualTo("Feature 1");
    }

    @Test
    void getConflicts_returnsOverloadedTeams() {
        List<Map<String, Object>> expected = List.of(
            Map.of("teamId", "t1", "teamName", "Team Alpha", "assignmentCount", 5L)
        );
        when(jdbcTemplate.queryForList(org.mockito.ArgumentMatchers.contains("HAVING COUNT"))).thenReturn(expected);

        List<Map<String, Object>> result = dashboardService.getConflicts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("teamName")).isEqualTo("Team Alpha");
    }

    @Test
    void getConflicts_emptyWhenNoOverloads() {
        when(jdbcTemplate.queryForList(org.mockito.ArgumentMatchers.contains("HAVING COUNT"))).thenReturn(List.of());

        List<Map<String, Object>> result = dashboardService.getConflicts();

        assertThat(result).isEmpty();
    }

    @Test
    void getTeamCapacityView_delegatesToJdbcTemplate() {
        List<Map<String, Object>> expected = List.of(Map.of("team_id", "t1", "member_count", 3L));
        when(jdbcTemplate.queryForList("SELECT * FROM v_team_capacity")).thenReturn(expected);

        List<Map<String, Object>> result = dashboardService.getTeamCapacityView();

        assertThat(result).hasSize(1);
    }

    @Test
    void getFeatureSummaryView_delegatesToJdbcTemplate() {
        List<Map<String, Object>> expected = List.of(Map.of("feature_id", "f1", "title", "Test"));
        when(jdbcTemplate.queryForList("SELECT * FROM v_feature_summary")).thenReturn(expected);

        List<Map<String, Object>> result = dashboardService.getFeatureSummaryView();

        assertThat(result).hasSize(1);
    }

    @Test
    void getSprintLoadView_delegatesToJdbcTemplate() {
        List<Map<String, Object>> expected = List.of(Map.of("sprint_id", "s1", "load", 40.0));
        when(jdbcTemplate.queryForList("SELECT * FROM v_sprint_load")).thenReturn(expected);

        List<Map<String, Object>> result = dashboardService.getSprintLoadView();

        assertThat(result).hasSize(1);
    }
}
