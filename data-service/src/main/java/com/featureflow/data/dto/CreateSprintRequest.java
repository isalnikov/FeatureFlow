package com.featureflow.data.dto;

import java.time.LocalDate;
import java.util.Map;

public record CreateSprintRequest(
    LocalDate startDate,
    LocalDate endDate,
    Map<String, Object> capacityOverrides,
    String externalId
) {}
