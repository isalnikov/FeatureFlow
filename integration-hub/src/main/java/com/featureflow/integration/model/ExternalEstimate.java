package com.featureflow.integration.model;

public record ExternalEstimate(
    Double storyPoints,
    Double originalEstimateHours,
    Double remainingEstimateHours
) {}
