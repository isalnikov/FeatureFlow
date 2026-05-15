package com.featureflow.integration.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record ExternalIssue(
    String externalId, String key, String title, String description,
    String status, String issueType, String priority,
    List<String> labels, String assignee, List<String> componentNames,
    ExternalEstimate estimate, List<String> dependencyKeys,
    LocalDate created, LocalDate updated, LocalDate dueDate
) {}
