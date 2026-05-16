package com.featureflow.integration.connector.jira.model;

import java.util.List;

public record JiraSearchResponse(
    List<JiraIssue> issues,
    int total,
    int maxResults
) {}
