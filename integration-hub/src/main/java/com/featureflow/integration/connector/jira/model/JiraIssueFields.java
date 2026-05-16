package com.featureflow.integration.connector.jira.model;

import java.util.List;

public record JiraIssueFields(
    String summary,
    String description,
    JiraStatus status,
    JiraPriority priority,
    List<JiraLabel> labels,
    JiraAssignee assignee,
    List<JiraComponent> components,
    String customfield_10016,
    List<JiraIssueLink> issuelinks,
    String created,
    String updated,
    String duedate,
    JiraProject project
) {}
