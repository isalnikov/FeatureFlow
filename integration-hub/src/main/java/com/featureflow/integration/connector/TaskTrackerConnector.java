package com.featureflow.integration.connector;

import com.featureflow.integration.model.*;
import java.util.List;

public interface TaskTrackerConnector {

    String getType();

    boolean testConnection(ConnectorConfig config);

    List<ExternalIssue> importFeatures(ConnectorConfig config, ImportFilter filter);

    List<ExternalSprint> importSprints(ConnectorConfig config, String projectId);

    List<ExternalTeam> importTeams(ConnectorConfig config, String projectId);

    ExportResult exportAssignments(ConnectorConfig config, List<AssignmentExport> assignments);

    void updateStatuses(ConnectorConfig config, List<StatusUpdate> updates);
}
