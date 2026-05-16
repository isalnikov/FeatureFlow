package com.featureflow.data.dto;

import com.featureflow.domain.valueobject.ClassOfService;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record CreateFeatureRequest(
    String title,
    String description,
    Double businessValue,
    UUID requestorId,
    LocalDate deadline,
    ClassOfService classOfService,
    Set<UUID> productIds,
    Map<String, Double> effortEstimate,
    Map<String, Double> stochasticEstimate,
    Set<String> requiredExpertise,
    Boolean canSplit,
    String externalId,
    String externalSource
) {}
