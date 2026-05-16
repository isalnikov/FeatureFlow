package com.featureflow.integration.connector.jira.model;

public record JiraIssueLink(
    String id,
    JiraLinkedIssue inwardIssue,
    JiraLinkedIssue outwardIssue,
    JiraLinkType type
) {}
