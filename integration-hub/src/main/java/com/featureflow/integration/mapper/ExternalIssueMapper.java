package com.featureflow.integration.mapper;

import com.featureflow.domain.entity.FeatureRequest;
import com.featureflow.integration.connector.jira.model.*;
import com.featureflow.integration.model.ExternalEstimate;
import com.featureflow.integration.model.ExternalIssue;
import com.featureflow.integration.model.ExternalSprint;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Component
public class ExternalIssueMapper {

    private static final DateTimeFormatter JIRA_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public ExternalIssue toExternalIssue(JiraIssue jiraIssue) {
        if (jiraIssue == null) return null;

        JiraIssueFields fields = jiraIssue.fields();
        if (fields == null) return null;

        Double storyPoints = parseDouble(fields.customfield_10016());

        List<String> dependencyKeys = fields.issuelinks() != null
            ? fields.issuelinks().stream()
                .filter(link -> link.inwardIssue() != null)
                .map(link -> link.inwardIssue().key())
                .toList()
            : List.of();

        return new ExternalIssue(
            jiraIssue.id(),
            jiraIssue.key(),
            fields.summary(),
            fields.description(),
            fields.status() != null ? fields.status().name() : null,
            null,
            fields.priority() != null ? fields.priority().name() : null,
            fields.labels() != null ? fields.labels().stream().map(JiraLabel::name).toList() : List.of(),
            fields.assignee() != null ? fields.assignee().displayName() : null,
            fields.components() != null ? fields.components().stream().map(JiraComponent::name).toList() : List.of(),
            new ExternalEstimate(storyPoints, null, null),
            dependencyKeys,
            parseDate(fields.created()),
            parseDate(fields.updated()),
            parseDate(fields.duedate())
        );
    }

    public ExternalSprint toExternalSprint(JiraSprint jiraSprint) {
        if (jiraSprint == null) return null;
        return new ExternalSprint(
            jiraSprint.id().toString(),
            jiraSprint.name(),
            parseDate(jiraSprint.startDate()),
            parseDate(jiraSprint.endDate()),
            jiraSprint.state(),
            jiraSprint.goal()
        );
    }

    public FeatureRequest toFeatureRequest(ExternalIssue externalIssue) {
        if (externalIssue == null) {
            return null;
        }

        FeatureRequest feature = new FeatureRequest(
                UUID.randomUUID(),
                externalIssue.title(),
                externalIssue.description(),
                0.0
        );

        if (externalIssue.dueDate() != null) {
            feature.setDeadline(externalIssue.dueDate());
        }

        return feature;
    }

    public ExternalIssue fromFeatureRequest(FeatureRequest featureRequest) {
        if (featureRequest == null) {
            return null;
        }

        return new ExternalIssue(
                featureRequest.getId().toString(),
                null,
                featureRequest.getTitle(),
                featureRequest.getDescription(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                featureRequest.getDeadline()
        );
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            if (dateStr.length() == 10) {
                return LocalDate.parse(dateStr);
            }
            return LocalDate.parse(dateStr.substring(0, 10));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
