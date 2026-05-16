package com.featureflow.data.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ProductDto(
    UUID id,
    String name,
    String description,
    Set<String> technologyStack,
    Integer version,
    Instant createdAt,
    Instant updatedAt,
    String createdBy,
    String updatedBy
) {}
