package com.featureflow.integration.connector.jira.model;

import java.util.Map;

public record JiraUpdateRequest(Map<String, Object> fields) {}
