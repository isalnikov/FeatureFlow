package com.featureflow.integration.service;

import com.featureflow.integration.connector.TaskTrackerConnector;
import com.featureflow.integration.model.ConnectorConfig;
import com.featureflow.integration.model.ExternalIssue;
import com.featureflow.integration.model.ExternalSprint;
import com.featureflow.integration.model.ExternalTeam;
import com.featureflow.integration.model.ImportFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SyncOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(SyncOrchestrator.class);

    private final Map<String, TaskTrackerConnector> connectorMap;
    private final ImportService importService;
    private final ExportService exportService;

    public SyncOrchestrator(List<TaskTrackerConnector> connectors,
                           ImportService importService,
                           ExportService exportService) {
        this.connectorMap = connectors.stream()
            .collect(Collectors.toMap(TaskTrackerConnector::getType, c -> c));
        this.importService = importService;
        this.exportService = exportService;
    }

    public SyncResult fullSync() {
        long startTime = System.currentTimeMillis();
        int totalSynced = 0;
        int totalErrors = 0;

        for (Map.Entry<String, TaskTrackerConnector> entry : connectorMap.entrySet()) {
            String type = entry.getKey();
            TaskTrackerConnector connector = entry.getValue();

            try {
                SyncResult connectorResult = syncConnector(type, connector);
                totalSynced += connectorResult.synced();
                totalErrors += connectorResult.errors();
            } catch (Exception e) {
                log.error("Sync failed for connector {}: {}", type, e.getMessage());
                totalErrors++;
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        return new SyncResult(totalSynced, totalErrors, duration);
    }

    private SyncResult syncConnector(String type, TaskTrackerConnector connector) {
        int synced = 0;
        int errors = 0;

        ConnectorConfig config = getDefaultConfig(type);
        if (config == null || !connector.testConnection(config)) {
            log.warn("Connector {} not configured or connection failed", type);
            return new SyncResult(0, 1, 0L);
        }

        try {
            ImportFilter filter = new ImportFilter(null, null, null, null, 100);

            List<ExternalIssue> issues = connector.importFeatures(config, filter);
            synced += issues.size();
            log.info("Imported {} features from {}", issues.size(), type);

            List<ExternalSprint> sprints = connector.importSprints(config, filter.projectKey());
            synced += sprints.size();
            log.info("Imported {} sprints from {}", sprints.size(), type);

            List<ExternalTeam> teams = connector.importTeams(config, filter.projectKey());
            synced += teams.size();
            log.info("Imported {} teams from {}", teams.size(), type);

        } catch (Exception e) {
            log.error("Error syncing {}: {}", type, e.getMessage());
            errors++;
        }

        return new SyncResult(synced, errors, 0L);
    }

    private ConnectorConfig getDefaultConfig(String type) {
        String baseUrl = System.getenv(type.toUpperCase() + "_BASE_URL");
        String username = System.getenv(type.toUpperCase() + "_USERNAME");
        String apiToken = System.getenv(type.toUpperCase() + "_API_TOKEN");
        String authToken = System.getenv(type.toUpperCase() + "_AUTH_TOKEN");

        if (baseUrl == null) return null;

        return new ConnectorConfig(baseUrl, "basic", username, apiToken, authToken);
    }

    public record SyncResult(int synced, int errors, long duration) {}
}
