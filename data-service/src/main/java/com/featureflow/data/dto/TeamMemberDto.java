package com.featureflow.data.dto;

import java.util.UUID;

public record TeamMemberDto(
    UUID personId,
    String name,
    String role,
    double availabilityPercent
) {}
