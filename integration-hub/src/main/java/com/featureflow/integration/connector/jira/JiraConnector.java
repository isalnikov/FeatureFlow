package com.featureflow.integration.connector.jira;

import com.featureflow.integration.connector.TaskTrackerConnector;
import com.featureflow.integration.connector.jira.model.*;
import com.featureflow.integration.mapper.ExternalIssueMapper;
import com.featureflow.integration.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JiraConnector implements TaskTrackerConnector {

    private static final String SOURCE_TYPE = "jira";

    private final JiraClient jiraClient;
    private final ExternalIssueMapper mapper;

    public JiraConnector(JiraClient jiraClient, ExternalIssueMapper mapper) {
        this.jiraClient = jiraClient;
        this.mapper = mapper;
    }

    @Override
    public String getType() {
        return SOURCE_TYPE;
    }

    @Override
    public boolean testConnection(ConnectorConfig config) {
        try {
            jiraClient.getMyself(config);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<ExternalIssue> importFeatures(ConnectorConfig config, ImportFilter filter) {
        String jql = buildJql(filter);
        List<JiraIssue> issues = jiraClient.searchIssues(config, jql, filter.maxResults());
        return issues.stream()
            .map(mapper::toExternalIssue)
            .toList();
    }

    @Override
    public List<ExternalSprint> importSprints(ConnectorConfig config, String projectId) {
        List<JiraBoard> boards = jiraClient.getBoards(config, projectId);
        if (boards.isEmpty()) return List.of();

        String boardId = boards.get(0).id().toString();
        List<JiraSprint> sprints = jiraClient.getSprints(config, boardId);
        return sprints.stream()
            .map(mapper::toExternalSprint)
            .toList();
    }

    @Override
    public List<ExternalTeam> importTeams(ConnectorConfig config, String projectId) {
        return List.of();
    }

    @Override
    public ExportResult exportAssignments(ConnectorConfig config, List<AssignmentExport> assignments) {
        List<ExportResult.Entry> results = new ArrayList<>();
        for (AssignmentExport assignment : assignments) {
            try {
                var request = new JiraUpdateRequest(Map.of(
                    "sprint", assignment.targetSprintName()
                ));
                jiraClient.updateIssue(config, assignment.externalIssueKey(), request);
                results.add(new ExportResult.Entry(assignment.externalIssueKey(), true, null));
            } catch (Exception e) {
                results.add(new ExportResult.Entry(assignment.externalIssueKey(), false, e.getMessage()));
            }
        }
        return new ExportResult(results);
    }

    @Override
    public void updateStatuses(ConnectorConfig config, List<StatusUpdate> updates) {
        for (StatusUpdate update : updates) {
            var request = new JiraUpdateRequest(Map.of(
                "status", Map.of("name", update.newStatus())
            ));
            jiraClient.updateIssue(config, update.externalIssueKey(), request);
        }
    }

    private String buildJql(ImportFilter filter) {
        StringBuilder jql = new StringBuilder();
        jql.append("project = ").append(filter.projectKey());

        if (filter.issueTypes() != null && !filter.issueTypes().isEmpty()) {
            jql.append(" AND issuetype IN (").append(String.join(",", filter.issueTypes())).append(")");
        }

        if (filter.statuses() != null && !filter.statuses().isEmpty()) {
            jql.append(" AND status IN (").append(String.join(",", filter.statuses())).append(")");
        }

        if (filter.sinceDate() != null) {
            jql.append(" AND updated >= '").append(filter.sinceDate()).append("'");
        }

        return jql.toString();
    }
}
