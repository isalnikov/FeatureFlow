package com.featureflow.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateAssignmentRequest(
    Map<String, Double> allocatedEffort
) {}
