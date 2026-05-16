package com.featureflow.data.dto;

import com.featureflow.domain.valueobject.ClassOfService;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateFeatureRequest(
    String title,
    String description,
    Double businessValue,
    LocalDate deadline,
    ClassOfService classOfService,
    Map<String, Double> effortEstimate,
    Map<String, Double> stochasticEstimate,
    Set<String> requiredExpertise,
    Boolean canSplit
) {}
