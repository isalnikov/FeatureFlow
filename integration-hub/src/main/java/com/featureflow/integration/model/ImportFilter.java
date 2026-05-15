package com.featureflow.integration.model;
import java.time.LocalDate;
import java.util.List;
public record ImportFilter(String projectKey, List<String> issueTypes, List<String> statuses, LocalDate sinceDate, int maxResults) {}
