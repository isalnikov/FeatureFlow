package com.featureflow.data.dto;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record SprintDto(
    UUID id,
    UUID planningWindowId,
    LocalDate startDate,
    LocalDate endDate,
    Map<String, Object> capacityOverrides,
    String externalId
) {}
