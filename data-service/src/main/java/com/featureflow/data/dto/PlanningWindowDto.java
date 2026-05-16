package com.featureflow.data.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PlanningWindowDto(
    UUID id,
    LocalDate startDate,
    LocalDate endDate,
    Integer version,
    Instant createdAt
) {}
