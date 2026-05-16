package com.featureflow.integration.service;

import com.featureflow.integration.connector.TaskTrackerConnector;
import com.featureflow.integration.model.AssignmentExport;
import com.featureflow.integration.model.ConnectorConfig;
import com.featureflow.integration.model.ExportResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExportService {

    private final Map<String, TaskTrackerConnector> connectorMap;

    public ExportService(List<TaskTrackerConnector> connectors) {
        this.connectorMap = connectors.stream()
            .collect(Collectors.toMap(TaskTrackerConnector::getType, c -> c));
    }

    public ExportResult exportTo(String source, ConnectorConfig config, List<AssignmentExport> assignments) {
        var connector = connectorMap.get(source);
        if (connector == null) {
            throw new IllegalArgumentException("Unknown connector type: " + source);
        }
        return connector.exportAssignments(config, assignments);
    }
}
