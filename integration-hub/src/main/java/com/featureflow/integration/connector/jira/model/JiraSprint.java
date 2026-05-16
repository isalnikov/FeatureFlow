package com.featureflow.integration.connector.jira.model;

public record JiraSprint(
    Long id,
    String name,
    String state,
    String startDate,
    String endDate,
    String goal
) {}
