package com.featureflow.integration.mapper;

import com.featureflow.domain.entity.FeatureRequest;
import com.featureflow.integration.connector.jira.model.*;
import com.featureflow.integration.model.ExternalIssue;
import com.featureflow.integration.model.ExternalSprint;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalIssueMapperTest {

    private final ExternalIssueMapper mapper = new ExternalIssueMapper();

    @Test
    void toExternalIssue_mapsAllFields() {
        JiraIssue jiraIssue = new JiraIssue("10001", "PROJ-1",
            new JiraIssueFields("Test Issue", "Description", new JiraStatus("Done", "10001"),
                new JiraPriority("High"), List.of(new JiraLabel("backend")),
                new JiraAssignee("Alice", "acc-1"), List.of(new JiraComponent("API")),
                "8.0", List.of(), "2025-01-01T00:00:00.000+0000",
                "2025-01-15T00:00:00.000+0000", "2025-02-01", new JiraProject("PROJ", "Test")));

        ExternalIssue result = mapper.toExternalIssue(jiraIssue);

        assertThat(result.externalId()).isEqualTo("10001");
        assertThat(result.key()).isEqualTo("PROJ-1");
        assertThat(result.title()).isEqualTo("Test Issue");
        assertThat(result.description()).isEqualTo("Description");
        assertThat(result.status()).isEqualTo("Done");
        assertThat(result.priority()).isEqualTo("High");
        assertThat(result.labels()).containsExactly("backend");
        assertThat(result.assignee()).isEqualTo("Alice");
        assertThat(result.componentNames()).containsExactly("API");
        assertThat(result.estimate().storyPoints()).isEqualTo(8.0);
    }

    @Test
    void toExternalIssue_nullIssue_returnsNull() {
        assertThat(mapper.toExternalIssue(null)).isNull();
    }

    @Test
    void toExternalIssue_nullFields_returnsNull() {
        JiraIssue jiraIssue = new JiraIssue("10001", "PROJ-1", null);
        assertThat(mapper.toExternalIssue(jiraIssue)).isNull();
    }

    @Test
    void toExternalSprint_mapsAllFields() {
        JiraSprint jiraSprint = new JiraSprint(1L, "Sprint 1", "active",
            "2025-01-01", "2025-01-14", "Deliver feature X");

        ExternalSprint result = mapper.toExternalSprint(jiraSprint);

        assertThat(result.externalId()).isEqualTo("1");
        assertThat(result.name()).isEqualTo("Sprint 1");
        assertThat(result.state()).isEqualTo("active");
        assertThat(result.goal()).isEqualTo("Deliver feature X");
    }

    @Test
    void toExternalSprint_nullSprint_returnsNull() {
        assertThat(mapper.toExternalSprint(null)).isNull();
    }

    @Test
    void toFeatureRequest_mapsExternalIssue() {
        ExternalIssue externalIssue = new ExternalIssue("1", "PROJ-1", "Test", "Desc",
            "Done", null, "High", List.of(), "Alice", List.of(),
            null, List.of(),
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 15), LocalDate.of(2025, 2, 1));

        FeatureRequest result = mapper.toFeatureRequest(externalIssue);

        assertThat(result.getTitle()).isEqualTo("Test");
        assertThat(result.getDescription()).isEqualTo("Desc");
        assertThat(result.getDeadline()).isEqualTo(LocalDate.of(2025, 2, 1));
    }

    @Test
    void toFeatureRequest_nullIssue_returnsNull() {
        assertThat(mapper.toFeatureRequest(null)).isNull();
    }

    @Test
    void fromFeatureRequest_mapsToExternalIssue() {
        FeatureRequest feature = new FeatureRequest(
            java.util.UUID.randomUUID(), "Test Feature", "Description", 50.0);
        feature.setDeadline(LocalDate.of(2025, 3, 1));

        ExternalIssue result = mapper.fromFeatureRequest(feature);

        assertThat(result.title()).isEqualTo("Test Feature");
        assertThat(result.description()).isEqualTo("Description");
        assertThat(result.dueDate()).isEqualTo(LocalDate.of(2025, 3, 1));
    }

    @Test
    void fromFeatureRequest_nullFeature_returnsNull() {
        assertThat(mapper.fromFeatureRequest(null)).isNull();
    }
}
