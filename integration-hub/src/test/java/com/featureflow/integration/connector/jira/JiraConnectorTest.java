package com.featureflow.integration.connector.jira;

import com.featureflow.integration.connector.jira.model.*;
import com.featureflow.integration.mapper.ExternalIssueMapper;
import com.featureflow.integration.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JiraConnectorTest {

    @Mock
    private JiraClient jiraClient;
    @Mock
    private ExternalIssueMapper mapper;

    private JiraConnector connector;

    @BeforeEach
    void setUp() {
        connector = new JiraConnector(jiraClient, mapper);
    }

    @Test
    void getType_returnsJira() {
        assertThat(connector.getType()).isEqualTo("jira");
    }

    @Test
    void testConnection_success() {
        ConnectorConfig config = new ConnectorConfig("https://test.atlassian.net", "basic", "user", "token", null);
        when(jiraClient.getMyself(config)).thenReturn(new JiraMyself("123", "Test User", "test@test.com"));

        assertThat(connector.testConnection(config)).isTrue();
    }

    @Test
    void testConnection_failure() {
        ConnectorConfig config = new ConnectorConfig("https://invalid.atlassian.net", "basic", "user", "token", null);
        when(jiraClient.getMyself(config)).thenThrow(new RuntimeException("Connection refused"));

        assertThat(connector.testConnection(config)).isFalse();
    }

    @Test
    void importFeatures_mapsJiraIssuesToExternalIssues() {
        ConnectorConfig config = new ConnectorConfig("https://test.atlassian.net", "basic", "user", "token", null);
        ImportFilter filter = new ImportFilter("PROJ", List.of("Story"), null, null, 50);

        JiraIssue jiraIssue = new JiraIssue("10001", "PROJ-1",
            new JiraIssueFields("Test Issue", "Description", new JiraStatus("Done", "10001"),
                new JiraPriority("High"), List.of(), null, List.of(), null, List.of(),
                "2025-01-01", "2025-01-15", "2025-02-01", new JiraProject("PROJ", "Test")));

        when(jiraClient.searchIssues(eq(config), any(String.class), eq(50))).thenReturn(List.of(jiraIssue));
        ExternalIssue expected = new ExternalIssue("10001", "PROJ-1", "Test Issue", "Description",
            "Done", null, "High", List.of(), null, List.of(), null, List.of(),
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 15), LocalDate.of(2025, 2, 1));
        when(mapper.toExternalIssue(jiraIssue)).thenReturn(expected);

        List<ExternalIssue> result = connector.importFeatures(config, filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).key()).isEqualTo("PROJ-1");
    }

    @Test
    void importSprints_mapsJiraSprintsToExternalSprints() {
        ConnectorConfig config = new ConnectorConfig("https://test.atlassian.net", "basic", "user", "token", null);

        when(jiraClient.getBoards(eq(config), eq("PROJ"))).thenReturn(List.of(new JiraBoard(1L, "Board 1", "scrum")));
        when(jiraClient.getSprints(eq(config), eq("1"))).thenReturn(List.of(
            new JiraSprint(1L, "Sprint 1", "active", "2025-01-01", "2025-01-14", "Goal")
        ));
        when(mapper.toExternalSprint(any(JiraSprint.class))).thenAnswer(invocation -> {
            JiraSprint s = invocation.getArgument(0);
            return new ExternalSprint(s.id().toString(), s.name(), LocalDate.parse(s.startDate()),
                LocalDate.parse(s.endDate()), s.state(), s.goal());
        });

        List<ExternalSprint> result = connector.importSprints(config, "PROJ");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Sprint 1");
    }

    @Test
    void importTeams_returnsEmptyList() {
        ConnectorConfig config = new ConnectorConfig("https://test.atlassian.net", "basic", "user", "token", null);
        assertThat(connector.importTeams(config, "PROJ")).isEmpty();
    }
}
