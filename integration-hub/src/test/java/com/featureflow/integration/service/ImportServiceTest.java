package com.featureflow.integration.service;

import com.featureflow.integration.connector.TaskTrackerConnector;
import com.featureflow.integration.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

    @Mock
    private TaskTrackerConnector mockConnector;

    private ImportService importService;

    @BeforeEach
    void setUp() {
        when(mockConnector.getType()).thenReturn("jira");
        importService = new ImportService(List.of(mockConnector));
    }

    @Test
    void importFrom_unknownConnector_throwsException() {
        ConnectorConfig config = new ConnectorConfig("https://test.com", "basic", "u", "t", null);
        assertThatThrownBy(() -> importService.importFrom("unknown", config))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown connector type");
    }

    @Test
    void importFrom_callsConnectorAndReturnsCounts() {
        ConnectorConfig config = new ConnectorConfig("https://test.atlassian.net", "basic", "u", "t", null);
        ImportFilter filter = new ImportFilter("PROJ", null, null, null, 50);

        ExternalIssue issue = new ExternalIssue("1", "PROJ-1", "Test", "", "Done", null, "High",
            List.of(), null, List.of(), null, List.of(), null, null, null);
        when(mockConnector.importFeatures(eq(config), any(ImportFilter.class))).thenReturn(List.of(issue));
        when(mockConnector.importSprints(eq(config), eq("PROJ"))).thenReturn(List.of());
        when(mockConnector.importTeams(eq(config), eq("PROJ"))).thenReturn(List.of());

        ImportResult result = importService.importFrom("jira", config, filter);

        assertThat(result.featuresCreated()).isEqualTo(1);
        assertThat(result.featuresUpdated()).isEqualTo(0);
        assertThat(result.sprintsCreated()).isEqualTo(0);
        assertThat(result.teamsCreated()).isEqualTo(0);
    }

    @Test
    void testConnection_delegatesToConnector() {
        ConnectorConfig config = new ConnectorConfig("https://test.atlassian.net", "basic", "u", "t", null);
        when(mockConnector.testConnection(config)).thenReturn(true);

        assertThat(importService.testConnection("jira", config)).isTrue();
    }

    @Test
    void testConnection_unknownConnector_returnsFalse() {
        ConnectorConfig config = new ConnectorConfig("https://test.atlassian.net", "basic", "u", "t", null);
        assertThat(importService.testConnection("unknown", config)).isFalse();
    }

    @Test
    void importFrom_withDefaultFilter() {
        ConnectorConfig config = new ConnectorConfig("https://test.atlassian.net", "basic", "u", "t", null);
        when(mockConnector.importFeatures(eq(config), any(ImportFilter.class))).thenReturn(List.of());
        when(mockConnector.importSprints(eq(config), any())).thenReturn(List.of());
        when(mockConnector.importTeams(eq(config), any())).thenReturn(List.of());

        ImportResult result = importService.importFrom("jira", config);

        assertThat(result).isNotNull();
    }
}
