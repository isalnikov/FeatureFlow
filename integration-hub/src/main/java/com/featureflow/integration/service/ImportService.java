package com.featureflow.integration.service;

import com.featureflow.integration.connector.TaskTrackerConnector;
import com.featureflow.integration.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImportService {

    private final Map<String, TaskTrackerConnector> connectorMap;

    public ImportService(List<TaskTrackerConnector> connectors) {
        this.connectorMap = connectors.stream()
            .collect(Collectors.toMap(TaskTrackerConnector::getType, c -> c));
    }

    public ImportResult importFrom(String source, ConnectorConfig config) {
        return importFrom(source, config, new ImportFilter(null, null, null, null, 100));
    }

    public ImportResult importFrom(String source, ConnectorConfig config, ImportFilter filter) {
        var connector = connectorMap.get(source);
        if (connector == null) {
            throw new IllegalArgumentException("Unknown connector type: " + source);
        }

        List<ExternalIssue> externalIssues = connector.importFeatures(config, filter);
        int featuresCreated = 0;
        int featuresUpdated = 0;

        for (var issue : externalIssues) {
            featuresCreated++;
        }

        List<ExternalSprint> externalSprints = connector.importSprints(config, filter.projectKey());
        int sprintsCreated = externalSprints.size();

        List<ExternalTeam> externalTeams = connector.importTeams(config, filter.projectKey());
        int teamsCreated = externalTeams.size();

        return new ImportResult(featuresCreated, featuresUpdated, sprintsCreated, teamsCreated);
    }

    public boolean testConnection(String source, ConnectorConfig config) {
        var connector = connectorMap.get(source);
        if (connector == null) return false;
        return connector.testConnection(config);
    }
}
