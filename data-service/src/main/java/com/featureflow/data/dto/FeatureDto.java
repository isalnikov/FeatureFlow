package com.featureflow.data.dto;

import com.featureflow.domain.valueobject.ClassOfService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record FeatureDto(
    UUID id,
    String title,
    String description,
    Double businessValue,
    UUID requestorId,
    LocalDate deadline,
    ClassOfService classOfService,
    Map<String, Double> effortEstimate,
    Map<String, Double> stochasticEstimate,
    Set<String> requiredExpertise,
    Boolean canSplit,
    String externalId,
    String externalSource,
    Integer version,
    Instant createdAt,
    Instant updatedAt,
    String createdBy,
    String updatedBy
) {}
