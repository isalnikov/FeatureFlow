package com.featureflow.integration.connector.jira;

import com.featureflow.integration.connector.TaskTrackerConnector;
import com.featureflow.integration.model.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Component
public class JiraConnector implements TaskTrackerConnector {

    private static final String SOURCE_TYPE = "jira";

    private final WebClient.Builder webClientBuilder;

    public JiraConnector(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public String getType() {
        return SOURCE_TYPE;
    }

    @Override
    public boolean testConnection(ConnectorConfig config) {
        return false;
    }

    @Override
    public List<ExternalIssue> importFeatures(ConnectorConfig config, ImportFilter filter) {
        return Collections.emptyList();
    }

    @Override
    public List<ExternalSprint> importSprints(ConnectorConfig config, String projectId) {
        return Collections.emptyList();
    }

    @Override
    public List<ExternalTeam> importTeams(ConnectorConfig config, String projectId) {
        return Collections.emptyList();
    }

    @Override
    public ExportResult exportAssignments(ConnectorConfig config, List<AssignmentExport> assignments) {
        return new ExportResult(Collections.emptyList());
    }

    @Override
    public void updateStatuses(ConnectorConfig config, List<StatusUpdate> updates) {
    }
}
