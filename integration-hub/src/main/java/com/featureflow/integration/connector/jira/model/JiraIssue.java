package com.featureflow.integration.connector.jira.model;

import java.util.List;

public record JiraIssue(
    String id,
    String key,
    JiraIssueFields fields
) {}
