package com.featureflow.data.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record TeamDto(
    UUID id,
    String name,
    Double focusFactor,
    Double bugReservePercent,
    Double techDebtReservePercent,
    Double velocity,
    Set<String> expertiseTags,
    String externalId,
    Integer version,
    Instant createdAt,
    Instant updatedAt,
    String createdBy,
    String updatedBy
) {}
